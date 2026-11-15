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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import party.morino.moripaactivityreport.common.model.report.SilentFlag

/**
 * 理由に含まれる「--silent」の解析を検証する
 */
class SilentFlagParserTest {
    @Test
    @DisplayName("Removes --silent from anywhere in the reason")
    fun parsesSilentFlag() {
        assertEquals(SilentFlag("長期間放置 close #2", true), SilentFlagParser.parse("長期間放置 --silent close #2"))
        assertEquals(SilentFlag("長期間放置", true), SilentFlagParser.parse("--silent 長期間放置"))
        assertEquals(SilentFlag("長期間放置", true), SilentFlagParser.parse("長期間放置　--SILENT"))
        assertEquals(SilentFlag("", true), SilentFlagParser.parse("--silent"))
    }

    @Test
    @DisplayName("Keeps the reason when --silent is not a separate word")
    fun ignoresOtherWords() {
        assertEquals(SilentFlag("長期間放置", false), SilentFlagParser.parse("長期間放置"))
        assertEquals(SilentFlag("--silently 放置", false), SilentFlagParser.parse("--silently 放置"))
        assertEquals(SilentFlag("放置--silent", false), SilentFlagParser.parse("放置--silent"))
        assertEquals(SilentFlag("", false), SilentFlagParser.parse(""))
    }
}
