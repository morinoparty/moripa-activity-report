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
import party.morino.moripaactivityreport.paper.claim.DeleteClaimSuggestions
import party.morino.moripaactivityreport.paper.model.ticket.TicketSuggestion
import party.morino.moripaactivityreport.paper.ticket.TicketTooltipFormatter
import java.time.Instant
import java.time.ZoneOffset

/**
 * /deleteclaim の理由の Tab 補完を検証する
 */
class DeleteClaimSuggestionsTest {
    private val tickets = listOf(TicketSuggestion(12, "ticket 12"), TicketSuggestion(3, "ticket 3"), TicketSuggestion(1, "ticket 1"))

    /** 補完の文字列だけを取り出す */
    private fun texts(arguments: String): List<String> = DeleteClaimSuggestions.suggest(arguments) { tickets }.map { it.text }

    @Test
    @DisplayName("Suggests close and --silent for the word being typed")
    fun suggestsKeywords() {
        assertEquals(listOf("close", "--silent"), texts(""))
        assertEquals(listOf("close"), texts("長期間放置 Cl"))
        assertEquals(listOf("--silent"), texts("長期間放置 --"))
        // 既に --silent を入力していれば候補にしない
        assertEquals(listOf("close"), texts("--silent 長期間放置 "))
        assertEquals(emptyList<String>(), texts("長期間放置"))
    }

    @Test
    @DisplayName("Suggests ticket ids with tooltips after close")
    fun suggestsTicketIds() {
        val suggestions = DeleteClaimSuggestions.suggest("長期間放置 close ") { tickets }
        assertEquals(listOf("#12", "#3", "#1"), suggestions.map { it.text })
        assertEquals("ticket 12", suggestions.first().tooltip)
        assertEquals(listOf("#12", "#1"), texts("長期間放置 close #1"))
        assertEquals(listOf("#3"), texts("長期間放置 CLOSE 3"))
        // 既に指定したチケットは候補にしない
        assertEquals(listOf("#3", "#1"), texts("close #12 close "))
        assertEquals(emptyList<String>(), texts("close abc"))
    }

    @Test
    @DisplayName("Does not fetch tickets unless a ticket id is being typed")
    fun fetchesTicketsLazily() {
        var fetched = false
        DeleteClaimSuggestions.suggest("長期間放置 cl") {
            fetched = true
            tickets
        }
        assertEquals(false, fetched)
    }

    @Test
    @DisplayName("Formats the ticket summary for the tooltip")
    fun formatsTooltip() {
        val createdAt = Instant.parse("2026-11-01T03:04:00Z")
        assertEquals(
            "griefing 2026/11/01 03:04 Steve: 家を壊されました 見に来てください",
            TicketTooltipFormatter.format("griefing", createdAt, "Steve", "家を壊されました\n見に来てください", ZoneOffset.UTC),
        )
        assertEquals("a".repeat(TicketTooltipFormatter.PREVIEW_LENGTH) + "…", TicketTooltipFormatter.preview("a".repeat(40)))
    }
}
