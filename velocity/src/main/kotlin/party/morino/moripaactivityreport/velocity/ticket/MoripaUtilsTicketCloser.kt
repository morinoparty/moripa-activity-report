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
package party.morino.moripaactivityreport.velocity.ticket

import party.morino.moripaactivityreport.common.model.ticket.TicketCloseDirective
import party.morino.moripaactivityreport.velocity.model.ticket.TicketCloseOperator
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
 * Velocity 版の MoripaUtils は、共有データベース (MySQL) を使う場合にだけチケットを扱える。
 *
 * @param logger クローズの失敗を記録するロガー
 */
class MoripaUtilsTicketCloser(
    private val logger: Logger,
) : TicketCloser {
    override fun close(operator: TicketCloseOperator, directive: TicketCloseDirective, feedback: (String) -> Unit) {
        val actor = TicketActor(operator.uuid, operator.name, operator.isStaff)
        val ticketApi = MoripaUtilsAPI.getInstance().ticket
        directive.ticketIds.forEach { ticketId ->
            ticketApi.closeTicket(actor, ticketId, TicketCloseReason.DONE, directive.comment)
                .whenComplete { result, error ->
                    if (error != null) {
                        // ticket 機能が無効 (MySQL を使っていない) 場合などは例外で完了する
                        logger.log(Level.WARNING, "Failed to close ticket #$ticketId", error)
                        feedback("<red>チケット #$ticketId をクローズできませんでした。")
                    } else {
                        feedback(resultMessage(ticketId, result))
                    }
                }
        }
    }

    /**
     * クローズの結果を伝えるメッセージを作る (外部状態に依存しない)
     *
     * @param ticketId チケットの番号
     * @param result クローズの結果
     * @return MiniMessage 形式のメッセージ
     */
    private fun resultMessage(ticketId: Long, result: TicketStatusChangeResult): String = when (result) {
        is TicketStatusChangeResult.Success -> "<green>チケット #$ticketId をクローズしました。"
        TicketStatusChangeResult.AlreadyClosed -> "<yellow>チケット #$ticketId は既にクローズされています。"
        TicketStatusChangeResult.TicketNotFound -> "<red>チケット #$ticketId が見つかりません。"
        TicketStatusChangeResult.NotPermitted -> "<red>チケット #$ticketId をクローズする権限がありません。"
        // コメントの検証などは理由が空でない以上ここでは起きないが、念のため伝える
        else -> "<red>チケット #$ticketId をクローズできませんでした。"
    }
}
