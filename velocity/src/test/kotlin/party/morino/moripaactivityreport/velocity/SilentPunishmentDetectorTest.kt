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
package party.morino.moripaactivityreport.velocity

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import party.morino.moripaactivityreport.velocity.punishment.SilentPunishmentDetector
import java.util.logging.Logger

/**
 * LibertyBans のイベントが silent かどうかの判定を検証する
 */
class SilentPunishmentDetectorTest {
    /** LibertyBans の PostPunishEventImpl と同じく isSilent() を持つイベント */
    class EventWithSilent(
        private val silent: Boolean,
    ) {
        fun isSilent(): Boolean = silent
    }

    /** isSilent() を持たないイベント (LibertyBans の実装が変わった場合) */
    class EventWithoutSilent

    private val detector = SilentPunishmentDetector(Logger.getLogger(SilentPunishmentDetectorTest::class.java.name))

    @Test
    @DisplayName("Reads isSilent() from the event implementation")
    fun readsIsSilent() {
        assertTrue(detector.isSilent(EventWithSilent(true)))
        assertFalse(detector.isSilent(EventWithSilent(false)))
    }

    @Test
    @DisplayName("Treats events without isSilent() as not silent")
    fun fallsBackToNotSilent() {
        assertFalse(detector.isSilent(EventWithoutSilent()))
    }
}
