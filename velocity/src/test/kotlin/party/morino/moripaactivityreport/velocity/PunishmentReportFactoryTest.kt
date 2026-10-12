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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import party.morino.moripaactivityreport.common.model.report.ReportActor
import party.morino.moripaactivityreport.common.model.report.ReportField
import party.morino.moripaactivityreport.velocity.punishment.PunishmentReportFactory
import space.arim.libertybans.api.PunishmentType
import java.time.Instant

/**
 * LibertyBans の処罰の報告の組み立てを検証する
 */
class PunishmentReportFactoryTest {
    private val target = ReportActor("Steve", null)
    private val start = Instant.parse("2026-10-12T12:00:00Z")

    @Test
    @DisplayName("Shows the punishment type, duration and reason")
    fun createsPunishmentReport() {
        // 7 日間の BAN は ISO-8601 の期間で表示する
        val ban = PunishmentReportFactory.createPunishment(null, target, PunishmentType.BAN, start, start.plusSeconds(7 * 24 * 3600), "荒らし")
        assertEquals(
            listOf(ReportField("処罰", "BAN"), ReportField("期間", "PT168H"), ReportField("理由", "荒らし")),
            ban.fields,
        )
        // 無期限の処罰は PT0S になる
        val permanent = PunishmentReportFactory.createPunishment(null, target, PunishmentType.WARN, start, null, "警告")
        assertEquals(ReportField("期間", "PT0S"), permanent.fields[1])
    }

    @Test
    @DisplayName("Shows the pardoned punishment type")
    fun createsPardonReport() {
        val pardon = PunishmentReportFactory.createPardon(null, target, PunishmentType.BAN)
        assertEquals(listOf(ReportField("恩赦", "BAN")), pardon.fields)
    }
}
