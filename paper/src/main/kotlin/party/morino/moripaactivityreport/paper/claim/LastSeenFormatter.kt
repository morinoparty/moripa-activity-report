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

import java.time.Duration
import java.time.Instant

/**
 * 最終ログインを「6日前」のような相対表記にする
 */
object LastSeenFormatter {
    /**
     * 最終ログインを表示用の文字列にする (外部状態に依存しない純粋関数)
     *
     * @param lastSeen 最後にサーバーにいた日時 (一度も参加していない、または分からない場合は null)
     * @param online 現在オンラインかどうか
     * @param now 現在日時
     * @return 表示用の文字列 (例: オンライン / 6日前 / 3時間前 / 不明)
     */
    fun format(lastSeen: Instant?, online: Boolean, now: Instant): String {
        if (online) return "オンライン"
        if (lastSeen == null) return "不明"
        // 時計のずれで未来の日時になっても負の値を表示しない
        val elapsed = Duration.between(lastSeen, now).coerceAtLeast(Duration.ZERO)
        return when {
            elapsed.toDays() > 0 -> "${elapsed.toDays()}日前"
            elapsed.toHours() > 0 -> "${elapsed.toHours()}時間前"
            elapsed.toMinutes() > 0 -> "${elapsed.toMinutes()}分前"
            else -> "たった今"
        }
    }
}
