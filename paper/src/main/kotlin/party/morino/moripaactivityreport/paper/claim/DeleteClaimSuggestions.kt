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
package party.morino.moripaactivityreport.paper.claim

import party.morino.moripaactivityreport.common.report.SilentFlagParser
import party.morino.moripaactivityreport.common.ticket.TicketCloseDirectiveParser
import party.morino.moripaactivityreport.paper.model.claim.ArgumentSuggestion
import party.morino.moripaactivityreport.paper.model.ticket.TicketSuggestion

/**
 * /deleteclaim の理由の Tab 補完の候補を決める
 *
 * Paper は入力中の語 (最後の空白より後) だけを候補で置き換えるため、候補も語ごとに出す。
 * - 「close」の直後の語: 「#番号」(tooltip にチケットの概要)
 * - それ以外の語: 「close」と「--silent」
 */
object DeleteClaimSuggestions {
    /** チケットのクローズの指示の先頭の語 */
    private const val CLOSE = "close"

    /** 「close」の tooltip */
    private const val CLOSE_TOOLTIP = "チケットをクローズする (close #番号)"

    /** 「--silent」の tooltip */
    private const val SILENT_TOOLTIP = "Discord に報告しない"

    /**
     * 候補を決める (チケットの取得以外は外部状態に依存しない)
     *
     * @param arguments /deleteclaim の後に入力された文字列 (コマンド名の後の空白より後)
     * @param tickets 候補にするチケットを返す関数 (「#番号」を補完するときだけ呼ぶ)
     * @return 候補 (入力中の語に前方一致するもの)
     */
    fun suggest(arguments: String, tickets: () -> List<TicketSuggestion>): List<ArgumentSuggestion> {
        val words = arguments.split(' ')
        val current = words.last()
        val previous = words.dropLast(1)
        if (previous.lastOrNull().equals(CLOSE, ignoreCase = true)) {
            return suggestTicketIds(current, previous.joinToString(" "), tickets)
        }
        return buildList {
            if (CLOSE.startsWith(current, ignoreCase = true)) add(ArgumentSuggestion(CLOSE, CLOSE_TOOLTIP))
            // 既に「--silent」を入力していれば候補にしない
            val alreadySilent = SilentFlagParser.parse(previous.joinToString(" ")).silent
            if (!alreadySilent && SilentFlagParser.FLAG.startsWith(current, ignoreCase = true)) {
                add(ArgumentSuggestion(SilentFlagParser.FLAG, SILENT_TOOLTIP))
            }
        }
    }

    /**
     * 「close」の直後に入力する「#番号」の候補を決める
     *
     * @param current 入力中の語
     * @param before 入力中の語より前の文字列 (既に指定したチケットを候補から除くために使う)
     * @param tickets 候補にするチケットを返す関数
     * @return 「#番号」の候補
     */
    private fun suggestTicketIds(
        current: String,
        before: String,
        tickets: () -> List<TicketSuggestion>,
    ): List<ArgumentSuggestion> {
        // 「#」を省いて数字から打ち始めた場合も一致させる
        val digits = current.removePrefix("#")
        if (!digits.all { it in '0'..'9' }) return emptyList()
        val specified = TicketCloseDirectiveParser.parse(before).ticketIds.toSet()
        return tickets()
            .filter { it.id !in specified && it.id.toString().startsWith(digits) }
            .map { ArgumentSuggestion("#${it.id}", it.tooltip) }
    }
}
