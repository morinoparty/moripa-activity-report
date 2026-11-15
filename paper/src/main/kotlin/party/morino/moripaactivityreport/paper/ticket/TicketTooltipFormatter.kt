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

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * チケット番号の Tab 補完の tooltip に表示する概要を組み立てる
 *
 * MoripaUtils の /ticket の補完 (TicketSummaryText) に倣う。
 * ただし候補は未対応のチケットだけなので対応状況は省き、公開 API からはカテゴリーの表示名が取れないため、カテゴリーは id で表示する。
 */
object TicketTooltipFormatter {
    /** 本文のプレビューの最大文字数 */
    const val PREVIEW_LENGTH: Int = 30

    /** 改行を含む連続した空白 */
    private val WHITESPACE: Regex = Regex("\\s+")

    /** 日時の表示形式 */
    private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")

    /**
     * tooltip に表示する概要を組み立てる (外部状態に依存しない純粋関数)
     *
     * @param categoryId カテゴリーの id
     * @param createdAt 作成日時
     * @param playerName 送信者の名前
     * @param content 本文
     * @param zone 表示に使うタイムゾーン
     * @return カテゴリー、作成日時、送信者、本文のプレビューを並べた文字列
     */
    fun format(
        categoryId: String,
        createdAt: Instant,
        playerName: String,
        content: String,
        zone: ZoneId = ZoneId.systemDefault(),
    ): String = "$categoryId ${DATE_FORMATTER.format(createdAt.atZone(zone))} $playerName: ${preview(content)}"

    /**
     * 本文の先頭を 1 行に収まるようにする (外部状態に依存しない純粋関数)
     *
     * 改行や連続する空白を 1 つの空白につぶし、[PREVIEW_LENGTH] 文字を超える部分は「…」で省略する。
     *
     * @param content 本文
     * @return 本文のプレビュー
     */
    fun preview(content: String): String {
        val flattened = content.trim().replace(WHITESPACE, " ")
        return if (flattened.length > PREVIEW_LENGTH) flattened.take(PREVIEW_LENGTH) + "…" else flattened
    }
}
