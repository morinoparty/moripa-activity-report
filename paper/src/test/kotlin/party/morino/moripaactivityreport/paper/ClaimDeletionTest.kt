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
package party.morino.moripaactivityreport.paper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import party.morino.moripaactivityreport.paper.claim.LastSeenFormatter
import java.time.Duration
import java.time.Instant

/**
 * 土地保護の削除の報告に使う最終ログインの表示を検証する
 */
class ClaimDeletionTest {
    @Test
    @DisplayName("Formats the last login relative to now")
    fun formatsLastSeen() {
        val now = Instant.parse("2026-10-12T12:00:00Z")
        assertEquals("6日前", LastSeenFormatter.format(now.minus(Duration.ofDays(6)), online = false, now = now))
        assertEquals("3時間前", LastSeenFormatter.format(now.minus(Duration.ofHours(3)), online = false, now = now))
        assertEquals("オンライン", LastSeenFormatter.format(now, online = true, now = now))
        assertEquals("不明", LastSeenFormatter.format(null, online = false, now = now))
    }
}
