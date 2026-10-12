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
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import party.morino.moripaactivityreport.common.model.config.WebhookConfig
import party.morino.moripaactivityreport.common.model.report.ActivityReport

/**
 * Discord の Execute Webhook API に送る本文を組み立てる
 *
 * 実行者を Webhook の送信者 (名前とアイコン)、対象者を Embed の author にし、各項目を縦に並べる。
 */
object DiscordWebhookPayload {
    /** Discord の Embed の項目の値の上限文字数 */
    private const val MAX_FIELD_VALUE_LENGTH = 1024

    /** Discord の Embed の項目名の上限文字数 */
    private const val MAX_FIELD_NAME_LENGTH = 256

    /** Webhook の送信者名の上限文字数 */
    private const val MAX_USERNAME_LENGTH = 80

    /**
     * 報告 1 件分の本文を組み立てる (外部状態に依存しない純粋関数)
     *
     * @param report 報告
     * @param config Webhook の設定 (アイコン画像 URL のテンプレートとサービスアカウントの表示を使う)
     * @return Discord の Execute Webhook API に送る JSON
     */
    fun create(report: ActivityReport, config: WebhookConfig): JsonObject = buildJsonObject {
        val executor = report.executor
        // プレイヤー以外 (コンソールなど) が実行した場合は、サービスアカウントとして表示する
        val username = executor?.name ?: config.serviceAccountName
        val avatarUrl = if (executor == null) {
            config.serviceAccountIconUrl.ifBlank { null }
        } else {
            AvatarUrlTemplate.resolve(config.avatarUrl, executor.name, executor.uuid)
        }
        put("username", username.take(MAX_USERNAME_LENGTH))
        avatarUrl?.let { put("avatar_url", it) }
        putJsonArray("embeds") {
            addJsonObject {
                putJsonObject("author") {
                    put("name", report.target.name)
                    AvatarUrlTemplate.resolve(config.avatarUrl, report.target.name, report.target.uuid)?.let { put("icon_url", it) }
                }
                put("color", report.color)
                putJsonArray("fields") {
                    report.fields.forEach { field ->
                        addJsonObject {
                            put("name", field.name.take(MAX_FIELD_NAME_LENGTH))
                            // 空の値は Discord に拒否されるため、ハイフンで埋める
                            put("value", field.value.ifBlank { "-" }.take(MAX_FIELD_VALUE_LENGTH))
                            put("inline", false)
                        }
                    }
                }
            }
        }
        // プレイヤー名などに @everyone が含まれていてもメンションしない
        putJsonObject("allowed_mentions") {
            putJsonArray("parse") {}
        }
    }
}
