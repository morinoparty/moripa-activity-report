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

import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.minimessage.MiniMessage
import party.morino.moripaactivityreport.common.model.report.ReportActor
import party.morino.moripaactivityreport.common.ticket.TicketCloseDirectiveParser
import party.morino.moripaactivityreport.common.webhook.ActivityReporter
import party.morino.moripaactivityreport.velocity.model.ticket.TicketCloseOperator
import party.morino.moripaactivityreport.velocity.ticket.TicketCloser
import space.arim.libertybans.api.CompositeVictim
import space.arim.libertybans.api.LibertyBans
import space.arim.libertybans.api.Operator
import space.arim.libertybans.api.PlayerOperator
import space.arim.libertybans.api.PlayerVictim
import space.arim.libertybans.api.PunishmentType
import space.arim.libertybans.api.Victim
import space.arim.libertybans.api.event.PostPardonEvent
import space.arim.libertybans.api.event.PostPunishEvent
import space.arim.libertybans.api.event.PunishEvent
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
 * LibertyBans の処罰と処罰の解除を Discord に報告し、理由に書かれたチケットをクローズする
 *
 * LibertyBans のクラスを参照するため、LibertyBans が導入されている場合にだけ生成すること。
 * イベントは LibertyBans が非同期に発行するため、名前の解決や送信もそのまま非同期で行う。
 *
 * 理由の「close #番号」は、処罰が確定する前 ([PunishEvent]) に理由から取り除き、LibertyBans には保存させない。
 * 取り除いた指示は預けておき、処罰が確定した後 ([PostPunishEvent]) にチケットをクローズする。
 *
 * @param server 処罰した人へ結果を伝えるためのプロキシ
 * @param reporter 報告の送信窓口
 * @param ticketCloser チケットをクローズする方法
 * @param logger 報告の失敗を記録するロガー
 */
class LibertyBansReporter(
    private val server: ProxyServer,
    private val reporter: ActivityReporter,
    private val ticketCloser: TicketCloser,
    private val logger: Logger,
) {
    private val omnibus: Omnibus = OmnibusProvider.getOmnibus()
    private val libertyBans: LibertyBans = omnibus.registry.getProvider(LibertyBans::class.java)
        .orElseThrow { IllegalStateException("LibertyBans is not registered in Omnibus") }

    /** 登録したリスナー (停止時に解除する) */
    private val listeners = mutableListOf<RegisteredListener>()

    /** 確定前の処罰の理由から取り出した、チケットのクローズの指示 */
    private val pendingTicketCloses = PendingTicketCloses()

    /**
     * 処罰と処罰の解除のイベントを購読する
     */
    fun start() {
        // 理由の書き換えは、他のリスナーが処罰を取り消すかどうかを判断する前に済ませる
        listeners += omnibus.eventBus.registerListener(PunishEvent::class.java, ListenerPriorities.HIGH) { event ->
            extractTicketCloses(event)
        }
        // 処罰が確定した後 (Post) のイベントだけを見て、取り消された処罰を報告しない
        listeners += omnibus.eventBus.registerListener(PostPunishEvent::class.java, ListenerPriorities.NORMAL) { event ->
            reportPunishment(event.punishment, event.target.orElse(null))
            closeTickets(event.punishment)
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
     * 処罰の理由から「close #番号」を取り除き、指示を確定後まで預ける
     *
     * @param event 確定前の処罰のイベント
     */
    private fun extractTicketCloses(event: PunishEvent) {
        if (event.isCancelled) return
        val draft = event.draftSanction
        val directive = TicketCloseDirectiveParser.parse(draft.reason)
        if (directive.ticketIds.isEmpty()) return
        // 指示を取り除いた残りを理由として LibertyBans に保存させる
        val reason = directive.comment.orEmpty()
        event.draftSanction = draft.toBuilder().reason(reason).build()
        pendingTicketCloses.put(PendingTicketCloses.keyOf(draft.type, draft.victim, draft.operator, reason), directive)
    }

    /**
     * 確定した処罰の理由に「close #番号」が書かれていた場合に、チケットをクローズする
     *
     * @param punishment 確定した処罰
     */
    private fun closeTickets(punishment: Punishment) {
        val key = PendingTicketCloses.keyOf(punishment.type, punishment.victim, punishment.operator, punishment.reason)
        val directive = pendingTicketCloses.take(key) ?: return
        resolveCloseOperator(punishment.operator).thenAccept { operator ->
            ticketCloser.close(operator, directive) { message -> sendFeedback(punishment.operator, message) }
        }.exceptionally { e ->
            logger.log(Level.WARNING, "Failed to close tickets for punishment #${punishment.identifier}", e)
            null
        }
    }

    /**
     * チケットをクローズする人を決める
     *
     * 処罰した人として扱う。プロキシにいれば MoripaUtils の運営権限で判定し、いなければ処罰の権限を持つ運営とみなす。
     * コンソールの場合は、サービスアカウントの名前で運営として扱う。
     *
     * @param operator 処罰した人
     * @return クローズする人
     */
    private fun resolveCloseOperator(operator: Operator): CompletableFuture<TicketCloseOperator> {
        if (operator !is PlayerOperator) {
            return CompletableFuture.completedFuture(TicketCloseOperator(null, reporter.config.serviceAccountName, isStaff = true))
        }
        val online = server.getPlayer(operator.uuid).orElse(null)
        val isStaff = online?.hasPermission(TICKET_STAFF_PERMISSION) ?: true
        return resolvePlayer(operator.uuid, online?.username)
            .thenApply { actor -> TicketCloseOperator(operator.uuid, actor.name, isStaff) }
    }

    /**
     * チケットのクローズの結果を、処罰した人に伝える
     *
     * プロキシにいないプレイヤーやコンソールの場合は、ログに残す。
     *
     * @param operator 処罰した人
     * @param message MiniMessage 形式のメッセージ
     */
    private fun sendFeedback(operator: Operator, message: String) {
        val player = (operator as? PlayerOperator)?.let { server.getPlayer(it.uuid).orElse(null) }
        if (player != null) {
            player.sendMessage(MINI_MESSAGE.deserialize(message))
        } else {
            logger.info(MINI_MESSAGE.stripTags(message))
        }
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
            .thenCombine(resolveVictim(punishment.victim, punishment.type, targetName)) { executor, target ->
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
            .thenCombine(resolveVictim(punishment.victim, punishment.type, targetName)) { executor, target ->
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
     * @param type 処罰の種類 (IP アドレスを伏せた表示に使う)
     * @param targetName コマンドで指定された対象の名前 (分からない場合は null)
     * @return 処罰された人 (IP アドレスの場合はアドレスを伏せ、アイコンは表示しない)
     */
    private fun resolveVictim(victim: Victim, type: PunishmentType, targetName: String?): CompletableFuture<ReportActor> {
        // IP アドレスは個人情報のため、コマンドで指定された文字列 (アドレスそのもの) も含めて表示しない
        val hidden = CompletableFuture.completedFuture(ReportActor(PunishmentReportFactory.hiddenAddressName(type), null))
        return when (victim) {
            is PlayerVictim -> resolvePlayer(victim.uuid, targetName)
            // UUID とアドレスの両方を対象にした処罰は、UUID が分かればプレイヤーとして表示する
            is CompositeVictim -> if (victim.uuid == CompositeVictim.WILDCARD_UUID) hidden else resolvePlayer(victim.uuid, null)
            else -> hidden
        }
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

    companion object {
        /** MoripaUtils の運営向けの権限 (すべてのチケットを扱える) */
        private const val TICKET_STAFF_PERMISSION = "moripautils.ticket.staff"

        /** 結果のメッセージの組み立てに使う MiniMessage */
        private val MINI_MESSAGE = MiniMessage.miniMessage()
    }
}
