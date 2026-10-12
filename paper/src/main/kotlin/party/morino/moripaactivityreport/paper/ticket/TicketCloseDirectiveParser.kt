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

import party.morino.moripaactivityreport.paper.model.ticket.TicketCloseDirective

/**
 * 削除の理由から「close #番号」の指示を取り出す
 *
 * 例: 「長期間放置 close #2」→ チケット #2 をクローズし、「長期間放置」をコメントとして残す。
 */
object TicketCloseDirectiveParser {
    /** 「close #2」「Close#2」などに一致する (大文字小文字は区別しない) */
    private val DIRECTIVE = Regex("""(?i)\bclose\s*#(\d+)""")

    /** 指示を取り除いた後に残る連続した空白 */
    private val WHITESPACE = Regex("""\s+""")

    /**
     * 理由を解析する (外部状態に依存しない純粋関数)
     *
     * @param reason /deleteclaim に入力された理由
     * @return クローズの指示。指示が無い場合は [TicketCloseDirective.ticketIds] が空になる
     */
    fun parse(reason: String): TicketCloseDirective {
        val ticketIds = DIRECTIVE.findAll(reason)
            // 桁数が多すぎて Long に収まらない番号は存在しないため無視する
            .mapNotNull { it.groupValues[1].toLongOrNull() }
            .distinct()
            .toList()
        val comment = reason.replace(DIRECTIVE, " ")
            .replace(WHITESPACE, " ")
            .trim()
            .ifEmpty { null }
        return TicketCloseDirective(ticketIds, comment)
    }
}
