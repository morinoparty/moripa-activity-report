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
import party.morino.moripaactivityreport.paper.model.ticket.TicketCloseDirective
import party.morino.moripaactivityreport.paper.ticket.TicketCloseDirectiveParser
import java.time.Duration
import java.time.Instant

/**
 * /deleteclaim の理由の解析と最終ログインの表示を検証する
 */
class ClaimDeletionTest {
    @Test
    @DisplayName("Extracts close directives and keeps the rest as the comment")
    fun parsesCloseDirectives() {
        assertEquals(TicketCloseDirective(listOf(2L), "長期間放置"), TicketCloseDirectiveParser.parse("長期間放置 close #2"))
        // 複数の指示や大文字小文字の違いも受け付け、重複は 1 回にする
        assertEquals(TicketCloseDirective(listOf(3L, 5L), "荒らし"), TicketCloseDirectiveParser.parse("Close#3 荒らし close #5 close #3"))
        // 指示しか無い場合はコメントを残さない
        assertEquals(TicketCloseDirective(listOf(7L), null), TicketCloseDirectiveParser.parse("close #7"))
        assertEquals(TicketCloseDirective(emptyList(), "長期間放置"), TicketCloseDirectiveParser.parse("長期間放置"))
    }

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
