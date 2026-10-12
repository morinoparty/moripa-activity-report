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
package party.morino.moripaactivityreport.common.model.ticket

/**
 * 理由 (/deleteclaim や LibertyBans の処罰の理由) に含まれていた、チケットをクローズする指示
 *
 * @property ticketIds クローズするチケットの番号 (書かれた順、重複なし)
 * @property comment 指示を取り除いた残りの理由 (チケットのコメントとして残す。何も残らない場合は null)
 */
data class TicketCloseDirective(
    val ticketIds: List<Long>,
    val comment: String?,
)
