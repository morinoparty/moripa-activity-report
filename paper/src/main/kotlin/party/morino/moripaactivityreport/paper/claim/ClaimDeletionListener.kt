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
package party.morino.moripaactivityreport.paper.claim

import me.ryanhamshire.GriefPrevention.Claim
import me.ryanhamshire.GriefPrevention.events.ClaimDeletedEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripaactivityreport.common.model.report.ReportActor
import party.morino.moripaactivityreport.common.webhook.ActivityReporter
import party.morino.moripaactivityreport.paper.model.claim.DeletedClaim
import party.morino.moripaactivityreport.paper.ticket.TicketCloseDirectiveParser
import party.morino.moripaactivityreport.paper.ticket.TicketCloser
import java.time.Instant

/**
 * GriefPrevention の /deleteclaim に理由を付けられるようにし、削除を Discord に報告する
 *
 * 削除そのものは GriefPrevention に任せる (サブディビジョンの警告や運営の土地保護の権限確認などをそのまま使うため)。
 * GriefPrevention は /deleteclaim の引数を無視するので、ここでは理由を記録してコマンドをそのまま通し、
 * コマンドの実行中に発生した [ClaimDeletedEvent] と結び付けて報告する。
 *
 * @param plugin 次の tick に処理を予約するためのプラグイン
 */
class ClaimDeletionListener(
    private val plugin: Plugin,
) : Listener, KoinComponent {
    private val reporter: ActivityReporter by inject()
    private val ticketCloser: TicketCloser by inject()

    /** 実行中の /deleteclaim (コマンドの実行中だけ値を持つ) */
    private var pending: PendingClaimDeletion? = null

    /**
     * /deleteclaim の理由を受け取る
     *
     * 理由は任意。省略された場合も削除は報告し、理由の欄は空 (「-」) にする。
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        // 先頭の / を除き、コマンド名と引数に分ける
        val parts = event.message.removePrefix("/").split(' ', limit = 2)
        if (parts.first().lowercase() !in DELETE_CLAIM_COMMANDS) return
        val player = event.player
        // 権限が無い場合は GriefPrevention にそのまま断らせる
        if (!player.hasPermission(DELETE_CLAIMS_PERMISSION)) return
        val reason = parts.getOrNull(1)?.trim().orEmpty()
        val deletion = PendingClaimDeletion(player, reason)
        pending = deletion
        // コマンドはこのイベントの直後に同じ tick で実行されるため、次の tick で削除結果をまとめて扱う
        Bukkit.getScheduler().runTask(plugin, Runnable { finish(deletion) })
    }

    /**
     * /deleteclaim の実行中に削除された土地保護を記録する
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onClaimDeleted(event: ClaimDeletedEvent) {
        pending?.deletedClaims?.add(event.claim)
    }

    /**
     * コマンドの実行が終わった後に、削除された土地保護を報告し、指示されたチケットをクローズする
     *
     * サブディビジョンを含む土地保護では 1 回目の /deleteclaim は警告だけで削除されないため、その場合は何もしない。
     *
     * @param deletion 実行された /deleteclaim の記録
     */
    private fun finish(deletion: PendingClaimDeletion) {
        // 次のコマンドの記録で上書きされていても、自分の記録だけを片付ける
        if (pending === deletion) pending = null
        val roots = deletion.rootClaims()
        if (roots.isEmpty()) return

        val executor = ReportActor(deletion.player.name, deletion.player.uniqueId)
        roots.forEach { claim ->
            reporter.report(ClaimDeletionReportFactory.create(executor, snapshot(claim), lastSeenOf(claim), deletion.reason))
        }

        val directive = TicketCloseDirectiveParser.parse(deletion.reason)
        if (directive.ticketIds.isNotEmpty()) {
            ticketCloser.close(deletion.player, directive)
        }
    }

    /**
     * 報告に使う値を土地保護から写し取る
     *
     * @param claim 削除された土地保護
     * @return 報告に使う情報
     */
    private fun snapshot(claim: Claim): DeletedClaim {
        val corner = claim.lesserBoundaryCorner
        return DeletedClaim(claim.ownerName, claim.ownerID, corner.blockX, corner.blockZ)
    }

    /**
     * 持ち主の最終ログインを表示用の文字列にする
     *
     * GriefPrevention が土地保護の期限切れの判定に使う値 (OfflinePlayer#getLastPlayed) と同じものを使う。
     *
     * @param claim 削除された土地保護
     * @return 表示用の文字列 (運営の土地保護は持ち主がいないため「-」)
     */
    @Suppress("DEPRECATION")
    private fun lastSeenOf(claim: Claim): String {
        val ownerId = claim.ownerID ?: return "-"
        val owner = Bukkit.getOfflinePlayer(ownerId)
        // 一度も参加していない場合は 0 が返る
        val lastSeen = owner.lastPlayed.takeIf { it > 0 }?.let(Instant::ofEpochMilli)
        return LastSeenFormatter.format(lastSeen, owner.isOnline, Instant.now())
    }

    companion object {
        /** 横取りするコマンド名 (GriefPrevention の名前空間付きも含む) */
        private val DELETE_CLAIM_COMMANDS = setOf("deleteclaim", "griefprevention:deleteclaim")

        /** GriefPrevention の /deleteclaim の権限 */
        private const val DELETE_CLAIMS_PERMISSION = "griefprevention.deleteclaims"
    }
}
