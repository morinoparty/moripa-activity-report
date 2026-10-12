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
package party.morino.moripaactivityreport.common.webhook

import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import party.morino.moripaactivityreport.common.model.config.WebhookConfig
import party.morino.moripaactivityreport.common.model.report.ActivityReport
import party.morino.moripaactivityreport.common.model.report.ReportActor
import party.morino.moripaactivityreport.common.model.report.ReportColor
import party.morino.moripaactivityreport.common.model.report.ReportField
import java.util.UUID

/**
 * Discord Webhook の本文の組み立てを検証する
 */
class DiscordWebhookPayloadTest {
    // 実在するプレイヤー (_NIKOMARU) の UUID を使う
    private val nikomaru = ReportActor("_NIKOMARU", UUID.fromString("f8b761ec-4a54-48eb-a040-c5604042bcc9"))
    private val target = ReportActor("Steve", UUID.fromString("8667ba71-b85a-4004-af54-457a9734eed7"))

    @Test
    @DisplayName("Shows the executor as the webhook sender and the target as the embed author")
    fun showsExecutorAndTarget() {
        val report = ActivityReport(nikomaru, target, ReportColor.BAN, listOf(ReportField("処罰", "BAN")))

        val payload = DiscordWebhookPayload.create(report, WebhookConfig())

        assertEquals("_NIKOMARU", payload["username"]!!.jsonPrimitive.content)
        assertEquals("https://api.mcheads.org/head/${nikomaru.uuid}/64", payload["avatar_url"]!!.jsonPrimitive.content)
        val embed = payload["embeds"]!!.jsonArray.single().jsonObject
        assertEquals("Steve", embed["author"]!!.jsonObject["name"]!!.jsonPrimitive.content)
        assertEquals("BAN", embed["fields"]!!.jsonArray.single().jsonObject["value"]!!.jsonPrimitive.content)
    }

    @Test
    @DisplayName("Shows the service account when the console executes")
    fun showsServiceAccountForConsole() {
        val report = ActivityReport(null, target, ReportColor.WARN, listOf(ReportField("処罰", "WARN")))

        val payload = DiscordWebhookPayload.create(report, WebhookConfig())

        assertEquals(WebhookConfig.DEFAULT_SERVICE_ACCOUNT_NAME, payload["username"]!!.jsonPrimitive.content)
        assertEquals(WebhookConfig.DEFAULT_SERVICE_ACCOUNT_ICON_URL, payload["avatar_url"]!!.jsonPrimitive.content)
    }
}
