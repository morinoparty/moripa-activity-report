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

/**
 * Tab 補完の候補にするチケットを集める (Strategy)
 *
 * MoripaUtils が導入されているかどうかで実装を切り替える。
 */
interface TicketSuggester {
    /**
     * プレイヤーがクローズできる未対応のチケットを新しい順に返す
     *
     * データベースへの問い合わせを待つため、メインスレッドからは呼ばないこと。
     *
     * @param player 補完を求めたプレイヤー
     * @return 候補にするチケット (取得できない場合は空)
     */
    fun openTickets(player: Player): List<TicketSuggestion>
}
