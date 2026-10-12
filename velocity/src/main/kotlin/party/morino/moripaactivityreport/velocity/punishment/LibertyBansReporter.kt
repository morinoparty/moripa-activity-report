/*
 * MoripaActivityReport
 * Copyright © 2026 morinoparty
 *
 * MoripaActivityReport is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * MoripaActivityReport is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with MoripaActivityReport. If not, see <https://www.gnu.org/licenses/>.
 */
package party.morino.moripaactivityreport.velocity.punishment

import party.morino.moripaactivityreport.common.model.report.ReportActor
import party.morino.moripaactivityreport.common.webhook.ActivityReporter
import space.arim.libertybans.api.AddressVictim
import space.arim.libertybans.api.CompositeVictim
import space.arim.libertybans.api.LibertyBans
import space.arim.libertybans.api.Operator
import space.arim.libertybans.api.PlayerOperator
import space.arim.libertybans.api.PlayerVictim
import space.arim.libertybans.api.Victim
import space.arim.libertybans.api.event.PostPardonEvent
import space.arim.libertybans.api.event.PostPunishEvent
import space.arim.libertybans.api.punish.Punishment
import space.arim.omnibus.Omnibus
import space.arim.omnibus.OmnibusProvider
import space.arim.omnibus.events.ListenerPriorities
import space.arim.omnibus.events.RegisteredListener
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.logging.Level
import java.util.logging.Logger

/**
 * LibertyBans の処罰と処罰の解除を Discord に報告する
 *
 * LibertyBans のクラスを参照するため、LibertyBans が導入されている場合にだけ生成すること。
 * イベントは LibertyBans が非同期に発行するため、名前の解決や送信もそのまま非同期で行う。
 *
 * @param reporter 報告の送信窓口
 * @param logger 報告の失敗を記録するロガー
 */
class LibertyBansReporter(
    private val reporter: ActivityReporter,
    private val logger: Logger,
) {
    private val omnibus: Omnibus = OmnibusProvider.getOmnibus()
    private val libertyBans: LibertyBans = omnibus.registry.getProvider(LibertyBans::class.java)
        .orElseThrow { IllegalStateException("LibertyBans is not registered in Omnibus") }

    /** 登録したリスナー (停止時に解除する) */
    private val listeners = mutableListOf<RegisteredListener>()

    /**
     * 処罰と処罰の解除のイベントを購読する
     */
    fun start() {
        // 処罰が確定した後 (Post) のイベントだけを見て、取り消された処罰を報告しない
        listeners += omnibus.eventBus.registerListener(PostPunishEvent::class.java, ListenerPriorities.NORMAL) { event ->
            reportPunishment(event.punishment, event.target.orElse(null))
        }
        listeners += omnibus.eventBus.registerListener(PostPardonEvent::class.java, ListenerPriorities.NORMAL) { event ->
            reportPardon(event.operator, event.punishment, event.target.orElse(null))
        }
    }

    /**
     * イベントの購読をやめる (プロキシの停止時や再読み込み時に呼ぶ)
     */
    fun stop() {
        listeners.forEach { omnibus.eventBus.unregisterListener(it) }
        listeners.clear()
    }

    /**
     * 処罰を報告する
     *
     * @param punishment 確定した処罰
     * @param targetName コマンドで指定された対象の名前 (分からない場合は null)
     */
    private fun reportPunishment(punishment: Punishment, targetName: String?) {
        // 無期限の処罰は終了日時を持たない
        val end = if (punishment.isPermanent) null else punishment.endDate
        resolveOperator(punishment.operator)
            .thenCombine(resolveVictim(punishment.victim, targetName)) { executor, target ->
                PunishmentReportFactory.createPunishment(executor, target, punishment.type, punishment.startDate, end, punishment.reason)
            }
            .thenCompose { reporter.report(it) }
            .exceptionally { e -> logger.log(Level.WARNING, "Failed to report punishment #${punishment.identifier}", e) }
    }

    /**
     * 処罰の解除 (恩赦) を報告する
     *
     * @param operator 解除した人
     * @param punishment 解除された処罰
     * @param targetName コマンドで指定された対象の名前 (分からない場合は null)
     */
    private fun reportPardon(operator: Operator, punishment: Punishment, targetName: String?) {
        resolveOperator(operator)
            .thenCombine(resolveVictim(punishment.victim, targetName)) { executor, target ->
                PunishmentReportFactory.createPardon(executor, target, punishment.type)
            }
            .thenCompose { reporter.report(it) }
            .exceptionally { e -> logger.log(Level.WARNING, "Failed to report pardon of punishment #${punishment.identifier}", e) }
    }

    /**
     * 処罰した人を報告に表示する形にする
     *
     * @param operator LibertyBans の操作者
     * @return プレイヤーならその名前と UUID、コンソールなら null (サービスアカウントとして表示する)
     */
    private fun resolveOperator(operator: Operator): CompletableFuture<ReportActor?> = when (operator) {
        is PlayerOperator -> resolvePlayer(operator.uuid, null).thenApply { it }
        else -> CompletableFuture.completedFuture(null)
    }

    /**
     * 処罰された人を報告に表示する形にする
     *
     * @param victim LibertyBans の処罰対象
     * @param targetName コマンドで指定された対象の名前 (分からない場合は null)
     * @return 処罰された人 (IP アドレスの場合はアドレスを名前にし、アイコンは表示しない)
     */
    private fun resolveVictim(victim: Victim, targetName: String?): CompletableFuture<ReportActor> = when (victim) {
        is PlayerVictim -> resolvePlayer(victim.uuid, targetName)
        // UUID とアドレスの両方を対象にした処罰は、UUID が分かればプレイヤーとして表示する
        is CompositeVictim ->
            if (victim.uuid == CompositeVictim.WILDCARD_UUID) {
                CompletableFuture.completedFuture(ReportActor(victim.address.toString(), null))
            } else {
                resolvePlayer(victim.uuid, targetName)
            }
        is AddressVictim -> CompletableFuture.completedFuture(ReportActor(targetName ?: victim.address.toString(), null))
        else -> CompletableFuture.completedFuture(ReportActor(targetName ?: victim.toString(), null))
    }

    /**
     * プレイヤーの名前を解決する
     *
     * @param uuid プレイヤーの UUID
     * @param knownName 既に分かっている名前 (分からない場合は null で、LibertyBans に問い合わせる)
     * @return プレイヤーの名前と UUID (名前が分からない場合は UUID を名前にする)
     */
    private fun resolvePlayer(uuid: UUID, knownName: String?): CompletableFuture<ReportActor> {
        if (knownName != null) return CompletableFuture.completedFuture(ReportActor(knownName, uuid))
        return libertyBans.userResolver.lookupName(uuid)
            .thenApply { name -> ReportActor(name.orElse(uuid.toString()), uuid) }
    }
}
