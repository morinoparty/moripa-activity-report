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

import org.bukkit.entity.Player
import party.morino.moripaactivityreport.paper.model.ticket.TicketSuggestion
import party.morino.moripautils.api.MoripaUtilsAPI
import party.morino.moripautils.api.model.ticket.TicketActor
import party.morino.moripautils.api.model.ticket.TicketListFilter
import party.morino.moripautils.api.model.ticket.TicketStatus
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.logging.Level
import java.util.logging.Logger

/**
 * MoripaUtils の公開 API から未対応のチケットを集める [TicketSuggester]
 *
 * MoripaUtils のクラスを参照するため、MoripaUtils が導入されている場合にだけ生成すること。
 *
 * @param logger 取得の失敗を記録するロガー
 */
class MoripaUtilsTicketSuggester(
    private val logger: Logger,
) : TicketSuggester {
    override fun openTickets(player: Player): List<TicketSuggestion> {
        // /ticket と同じ権限で判定されるよう、運営かどうかは MoripaUtils の権限で決める (運営以外は自分のチケットだけになる)
        val actor = TicketActor(player.uniqueId, player.name, player.hasPermission(TICKET_STAFF_PERMISSION))
        val future = MoripaUtilsAPI.getInstance().ticket
            .listTickets(actor, TicketListFilter(status = TicketStatus.OPEN), 0, MAX_SUGGESTIONS)
        val tickets = try {
            future.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
        } catch (e: TimeoutException) {
            // 補完が遅れるよりは候補を出さない
            future.cancel(false)
            return emptyList()
        } catch (e: ExecutionException) {
            logger.log(Level.WARNING, "Failed to list tickets for suggestions", e.cause ?: e)
            return emptyList()
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            return emptyList()
        }
        return tickets.map { ticket ->
            TicketSuggestion(
                ticket.id,
                TicketTooltipFormatter.format(ticket.categoryId, ticket.createdAt, ticket.playerName, ticket.content),
            )
        }
    }

    companion object {
        /** MoripaUtils の運営向けの権限 (すべてのチケットを扱える) */
        private const val TICKET_STAFF_PERMISSION = "moripautils.ticket.staff"

        /** 候補にするチケットの最大数 */
        private const val MAX_SUGGESTIONS = 20

        /** チケットの取得を待つ最大時間 */
        private const val TIMEOUT_MILLIS = 1_000L
    }
}
