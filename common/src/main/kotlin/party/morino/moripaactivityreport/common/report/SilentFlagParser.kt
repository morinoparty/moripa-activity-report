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
package party.morino.moripaactivityreport.common.report

import party.morino.moripaactivityreport.common.model.report.SilentFlag

/**
 * 理由 (/deleteclaim の理由など) から、報告を止める「--silent」を取り出す
 *
 * 例: 「長期間放置 --silent close #2」→ Discord に報告せず、理由は「長期間放置 close #2」とする。
 */
object SilentFlagParser {
    /** 報告を止める指定 */
    const val FLAG: String = "--silent"

    /** 空白 (全角スペースを含む) で区切られた「--silent」に一致する。「--silently」のような別の語には一致させない */
    private val SILENT = Regex("""(?U)(?<!\S)--silent(?!\S)""", RegexOption.IGNORE_CASE)

    /** 指定を取り除いた後に残る連続した空白 (全角スペースを含む) */
    private val WHITESPACE = Regex("""(?U)\s+""")

    /**
     * 理由を解析する (外部状態に依存しない純粋関数)
     *
     * @param reason 入力された理由
     * @return 「--silent」を取り除いた理由と、指定されていたかどうか
     */
    fun parse(reason: String): SilentFlag {
        if (!SILENT.containsMatchIn(reason)) return SilentFlag(reason.trim(), silent = false)
        val rest = reason.replace(SILENT, " ")
            .replace(WHITESPACE, " ")
            .trim()
        return SilentFlag(rest, silent = true)
    }
}
