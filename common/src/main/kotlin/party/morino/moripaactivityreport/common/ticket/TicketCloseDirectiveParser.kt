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
package party.morino.moripaactivityreport.common.ticket

import party.morino.moripaactivityreport.common.model.ticket.TicketCloseDirective

/**
 * 理由 (/deleteclaim や LibertyBans の処罰の理由) から「close #番号」の指示を取り出す
 *
 * 例: 「長期間放置 close #2」→ チケット #2 をクローズし、「長期間放置」をコメントとして残す。
 * 日本語入力のまま打たれることを考え、全角の英字 (ｃｌｏｓｅ)・全角スペース・全角の ＃・全角数字も受け付ける。
 */
object TicketCloseDirectiveParser {
    /**
     * 「close #2」「Close#2」「ｃｌｏｓｅ　＃２」などに一致する
     *
     * - 大文字小文字は区別しない (全角の英字も同じ扱いにする)
     * - (?U) により、空白には全角スペースが、数字には全角数字が含まれる
     * - 「disclose #2」のような英単語の一部には一致させないが、「放置close #2」のように日本語の直後なら一致させる
     */
    private val DIRECTIVE = Regex(
        """(?U)(?<![A-Za-z0-9Ａ-Ｚａ-ｚ０-９])[cCｃＣ][lLｌＬ][oOｏＯ][sSｓＳ][eEｅＥ]\s*[#＃](\d+)""",
    )

    /** 指示を取り除いた後に残る連続した空白 (全角スペースを含む) */
    private val WHITESPACE = Regex("""(?U)\s+""")

    /**
     * 理由を解析する (外部状態に依存しない純粋関数)
     *
     * @param reason /deleteclaim や処罰に入力された理由
     * @return クローズの指示。指示が無い場合は [TicketCloseDirective.ticketIds] が空になる
     */
    fun parse(reason: String): TicketCloseDirective {
        val ticketIds = DIRECTIVE.findAll(reason)
            // 桁数が多すぎて Long に収まらない番号は存在しないため無視する
            .mapNotNull { toTicketId(it.groupValues[1]) }
            .distinct()
            .toList()
        val comment = reason.replace(DIRECTIVE, " ")
            .replace(WHITESPACE, " ")
            .trim()
            .ifEmpty { null }
        return TicketCloseDirective(ticketIds, comment)
    }

    /**
     * 番号の文字列をチケットの番号にする (外部状態に依存しない純粋関数)
     *
     * @param digits 数字の並び (全角数字を含んでもよい)
     * @return チケットの番号。Long に収まらない場合は null
     */
    private fun toTicketId(digits: String): Long? =
        // 全角数字を半角に直してから変換する (Unicode の数字は digitToInt で 0〜9 になる)
        digits.map { it.digitToInt() }.joinToString("").toLongOrNull()
}
