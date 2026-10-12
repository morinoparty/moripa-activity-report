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
package party.morino.moripaactivityreport.paper.ticket

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.entity.Player
import party.morino.moripaactivityreport.common.model.ticket.TicketCloseDirective
import party.morino.moripautils.api.MoripaUtilsAPI
import party.morino.moripautils.api.model.ticket.TicketActor
import party.morino.moripautils.api.model.ticket.TicketCloseReason
import party.morino.moripautils.api.model.ticket.TicketStatusChangeResult
import java.util.logging.Level
import java.util.logging.Logger

/**
 * MoripaUtils の公開 API でチケットをクローズする [TicketCloser]
 *
 * MoripaUtils のクラスを参照するため、MoripaUtils が導入されている場合にだけ生成すること。
 *
 * @param logger クローズの失敗を記録するロガー
 */
class MoripaUtilsTicketCloser(
    private val logger: Logger,
) : TicketCloser {
    override fun close(player: Player, directive: TicketCloseDirective) {
        // /ticket と同じ権限で判定されるよう、運営かどうかは MoripaUtils の権限で決める
        val actor = TicketActor(player.uniqueId, player.name, player.hasPermission(TICKET_STAFF_PERMISSION))
        val ticketApi = MoripaUtilsAPI.getInstance().ticket
        directive.ticketIds.forEach { ticketId ->
            ticketApi.closeTicket(actor, ticketId, TicketCloseReason.DONE, directive.comment)
                .whenComplete { result, error ->
                    // Player#sendRichMessage はメインスレッド以外から呼んでもよい
                    if (error != null) {
                        logger.log(Level.WARNING, "Failed to close ticket #$ticketId", error)
                        player.sendRichMessage("<red>チケット #<id> をクローズできませんでした。", idPlaceholder(ticketId))
                    } else {
                        sendResult(player, ticketId, result)
                    }
                }
        }
    }

    /**
     * クローズの結果をプレイヤーに伝える
     *
     * @param player 削除を実行したプレイヤー
     * @param ticketId チケットの番号
     * @param result クローズの結果
     */
    private fun sendResult(player: Player, ticketId: Long, result: TicketStatusChangeResult) {
        val id = idPlaceholder(ticketId)
        when (result) {
            is TicketStatusChangeResult.Success -> player.sendRichMessage("<green>チケット #<id> をクローズしました。", id)
            TicketStatusChangeResult.AlreadyClosed -> player.sendRichMessage("<yellow>チケット #<id> は既にクローズされています。", id)
            TicketStatusChangeResult.TicketNotFound -> player.sendRichMessage("<red>チケット #<id> が見つかりません。", id)
            TicketStatusChangeResult.NotPermitted -> player.sendRichMessage("<red>チケット #<id> をクローズする権限がありません。", id)
            // 再オープン用の結果やコメントの検証は、理由が空でない以上ここでは起きないが、念のため伝える
            else -> player.sendRichMessage("<red>チケット #<id> をクローズできませんでした: <result>", id, Placeholder.unparsed("result", result.toString()))
        }
    }

    /**
     * チケット番号の MiniMessage のプレースホルダーを作る
     *
     * @param ticketId チケットの番号
     * @return <id> を置き換えるプレースホルダー
     */
    private fun idPlaceholder(ticketId: Long) = Placeholder.unparsed("id", ticketId.toString())

    companion object {
        /** MoripaUtils の運営向けの権限 (すべてのチケットを扱える) */
        private const val TICKET_STAFF_PERMISSION = "moripautils.ticket.staff"
    }
}
