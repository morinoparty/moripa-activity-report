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
 * 削除の理由に書かれたチケットをクローズする (Strategy)
 *
 * MoripaUtils が導入されているかどうかで実装を切り替える。
 */
interface TicketCloser {
    /**
     * 指示されたチケットをクローズし、結果をプレイヤーに伝える
     *
     * @param player 削除を実行したプレイヤー (クローズする人として扱う)
     * @param directive クローズの指示 (クローズするチケットが 1 つ以上あること)
     */
    fun close(player: Player, directive: TicketCloseDirective)
}
