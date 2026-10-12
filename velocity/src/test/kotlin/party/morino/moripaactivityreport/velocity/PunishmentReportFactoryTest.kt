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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import party.morino.moripaactivityreport.common.model.report.ReportActor
import party.morino.moripaactivityreport.common.model.report.ReportField
import party.morino.moripaactivityreport.common.model.ticket.TicketCloseDirective
import party.morino.moripaactivityreport.velocity.punishment.PendingTicketCloses
import party.morino.moripaactivityreport.velocity.punishment.PunishmentReportFactory
import space.arim.libertybans.api.PunishmentType
import java.time.Duration
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
        val ban = PunishmentReportFactory.createPunishment(null, target, PunishmentType.BAN, start, start.plusSeconds(7 * 24 * 3600), "荒らし")
        assertEquals(
            listOf(ReportField("処罰", "BAN"), ReportField("期間", "7日"), ReportField("理由", "荒らし")),
            ban.fields,
        )
        val permanent = PunishmentReportFactory.createPunishment(null, target, PunishmentType.WARN, start, null, "警告")
        assertEquals(ReportField("期間", "無期限"), permanent.fields[1])
        // キックは期間を持たないため、期間の欄を出さない
        val kick = PunishmentReportFactory.createPunishment(null, target, PunishmentType.KICK, start, null, "チュートリアル未完了")
        assertEquals(listOf("処罰", "理由"), kick.fields.map { it.name })
    }

    @Test
    @DisplayName("Formats the duration in Japanese units")
    fun formatsDuration() {
        assertEquals("1日 12時間", PunishmentReportFactory.formatDuration(start, start.plusSeconds(36 * 3600)))
        assertEquals("30分", PunishmentReportFactory.formatDuration(start, start.plusSeconds(30 * 60)))
    }

    @Test
    @DisplayName("Shows the pardoned punishment type")
    fun createsPardonReport() {
        val pardon = PunishmentReportFactory.createPardon(null, target, PunishmentType.BAN)
        assertEquals(listOf(ReportField("恩赦", "BAN")), pardon.fields)
    }

    @Test
    @DisplayName("Hides IP addresses")
    fun hidesAddress() {
        assertEquals("IP BANにつき非表示", PunishmentReportFactory.hiddenAddressName(PunishmentType.BAN))
    }

    @Test
    @DisplayName("Keeps ticket close directives until the punishment is enacted")
    fun keepsPendingTicketCloses() {
        val pending = PendingTicketCloses(Duration.ofMinutes(5))
        val directive = TicketCloseDirective(listOf(11L), "荒らし")
        val key = PendingTicketCloses.keyOf(PunishmentType.BAN, "victim", "operator", "荒らし")

        pending.put(key, directive, start)
        // 一度取り出した指示は消える
        assertEquals(directive, pending.take(key, start.plusSeconds(1)))
        assertNull(pending.take(key, start.plusSeconds(1)))

        // 確定しなかった処罰の指示は期限が切れると捨てる
        pending.put(key, directive, start)
        assertNull(pending.take(key, start.plus(Duration.ofMinutes(6))))
    }
}
