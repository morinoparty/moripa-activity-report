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

import kotlinx.serialization.json.JsonObject
import party.morino.moripaactivityreport.common.model.config.WebhookConfig
import party.morino.moripaactivityreport.common.model.report.ActivityReport
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.logging.Level
import java.util.logging.Logger

/**
 * 報告を Discord Webhook に送る
 *
 * 送信はバックグラウンドで行い、失敗してもゲームやプロキシの処理には影響させない (ログに残すだけにする)。
 *
 * @param config Webhook の設定
 * @param logger 送信の失敗を記録するロガー
 */
class DiscordWebhookSender(
    private val config: WebhookConfig,
    private val logger: Logger,
) {
    /** 接続を使い回すため、送信者ごとに 1 つだけ持つ */
    private val client: HttpClient = HttpClient.newBuilder()
        .connectTimeout(TIMEOUT)
        .build()

    /**
     * 報告を送る (完了は待たない)
     *
     * @param report 報告
     * @return 送信が終わると完了する Future (失敗してもログに残して正常に完了する)
     */
    fun send(report: ActivityReport): CompletableFuture<Unit> {
        // URL が未設定の場合は報告を送らない
        if (config.url.isBlank()) return CompletableFuture.completedFuture(Unit)
        return post(DiscordWebhookPayload.create(report, config))
    }

    /**
     * 本文を Webhook に POST する
     *
     * @param payload Discord の Execute Webhook API に送る JSON
     * @return 送信が終わると完了する Future
     */
    private fun post(payload: JsonObject): CompletableFuture<Unit> {
        val request = HttpRequest.newBuilder(URI.create(config.url))
            .timeout(TIMEOUT)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
            .build()
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .handle { response, error ->
                when {
                    error != null -> logger.log(Level.WARNING, "Failed to send activity report to Discord", error)
                    // Discord は成功時に 204 (wait=false) を返す
                    response.statusCode() !in SUCCESS_STATUS -> logger.warning(
                        "Discord webhook responded with ${response.statusCode()}: ${response.body()}",
                    )
                }
            }
    }

    companion object {
        /** 接続と応答の待ち時間の上限 */
        private val TIMEOUT: Duration = Duration.ofSeconds(10)

        /** 成功とみなす HTTP ステータス */
        private val SUCCESS_STATUS = 200..299
    }
}
