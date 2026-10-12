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
import party.morino.moripaactivityreport.paper.model.ticket.TicketCloseDirective

/**
 * MoripaUtils が導入されていない場合の [TicketCloser]
 *
 * チケットはクローズできないため、指示が無視されたことだけをプレイヤーに伝える。
 */
class UnavailableTicketCloser : TicketCloser {
    override fun close(player: Player, directive: TicketCloseDirective) {
        player.sendRichMessage("<yellow>MoripaUtils が導入されていないため、チケットはクローズしませんでした。")
    }
}
