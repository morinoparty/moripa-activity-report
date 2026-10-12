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

/**
 * 処罰の理由に書かれたチケットをクローズする (Strategy)
 *
 * MoripaUtils が導入されているかどうかで実装を切り替える。
 */
interface TicketCloser {
    /**
     * 指示されたチケットをクローズし、結果を伝える
     *
     * @param operator クローズする人
     * @param directive クローズの指示 (クローズするチケットが 1 つ以上あること)
     * @param feedback 結果を伝える処理 (MiniMessage 形式の文字列を受け取る)
     */
    fun close(operator: TicketCloseOperator, directive: TicketCloseDirective, feedback: (String) -> Unit)
}
