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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import party.morino.moripaactivityreport.common.model.ticket.TicketCloseDirective

/**
 * 理由に含まれる「close #番号」の解析を検証する
 */
class TicketCloseDirectiveParserTest {
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
    @DisplayName("Accepts full-width spaces, hash signs, digits and letters")
    fun parsesFullWidthDirectives() {
        assertEquals(TicketCloseDirective(listOf(2L), "長期間放置"), TicketCloseDirectiveParser.parse("長期間放置　close　＃２"))
        assertEquals(TicketCloseDirective(listOf(11L), "長期間放置"), TicketCloseDirectiveParser.parse("長期間放置ｃｌｏｓｅ＃１１"))
        // 英単語の一部は指示として扱わない
        assertEquals(TicketCloseDirective(emptyList(), "disclose #3"), TicketCloseDirectiveParser.parse("disclose #3"))
    }
}
