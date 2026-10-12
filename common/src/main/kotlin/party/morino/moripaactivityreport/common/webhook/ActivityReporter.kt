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

import party.morino.moripaactivityreport.common.model.config.WebhookConfig
import party.morino.moripaactivityreport.common.model.report.ActivityReport
import java.util.concurrent.CompletableFuture
import java.util.logging.Logger

/**
 * 各機能から報告を受け取り、現在の設定の Webhook に送る窓口 (Facade)
 *
 * 設定の再読み込み (/mar reload, /marv reload) で送信先が変わっても、各機能はこのインスタンスを使い続ければよい。
 *
 * @param logger 送信の失敗を記録するロガー
 */
class ActivityReporter(
    private val logger: Logger,
) {
    /** 現在の設定で作った送信者 (未設定の間は null で、報告を送らない) */
    @Volatile
    private var sender: DiscordWebhookSender? = null

    /**
     * 送信先の設定を反映する
     *
     * @param config 新しい Webhook の設定
     */
    fun apply(config: WebhookConfig) {
        sender = DiscordWebhookSender(config, logger)
        if (config.url.isBlank()) {
            logger.warning("webhook.url is empty; activity reports will not be sent")
        }
    }

    /**
     * 報告を送る (完了は待たない)
     *
     * @param report 報告
     * @return 送信が終わると完了する Future
     */
    fun report(report: ActivityReport): CompletableFuture<Unit> =
        sender?.send(report) ?: CompletableFuture.completedFuture(Unit)
}
