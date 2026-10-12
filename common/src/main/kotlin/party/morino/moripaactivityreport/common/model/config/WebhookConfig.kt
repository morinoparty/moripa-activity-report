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
package party.morino.moripaactivityreport.common.model.config

import kotlinx.serialization.Serializable

/**
 * 報告を送る Discord Webhook の設定
 *
 * @property url Discord Webhook の URL。空文字の場合は報告を送らない
 * @property avatarUrl プレイヤーのアイコン画像の URL。{uuid} と {name} はプレイヤーの UUID と名前に置き換える
 * @property serviceAccountName コンソールなどプレイヤー以外が実行した場合に、実行者として表示する名前
 * @property serviceAccountIconUrl コンソールなどプレイヤー以外が実行した場合に、実行者として表示するアイコン画像の URL
 */
@Serializable
data class WebhookConfig(
    val url: String = "",
    val avatarUrl: String = DEFAULT_AVATAR_URL,
    val serviceAccountName: String = DEFAULT_SERVICE_ACCOUNT_NAME,
    val serviceAccountIconUrl: String = DEFAULT_SERVICE_ACCOUNT_ICON_URL,
) {
    companion object {
        /** avatarUrl の既定値 (MC Heads の頭部画像。名前が変わっても同じ画像になるよう UUID で引く) */
        const val DEFAULT_AVATAR_URL: String = "https://api.mcheads.org/head/{uuid}/64"

        /** serviceAccountName の既定値 */
        const val DEFAULT_SERVICE_ACCOUNT_NAME: String = "Moripa Service Account"

        /** serviceAccountIconUrl の既定値 (MoripaUtils のチケット通知と同じ morinoparty のロゴ) */
        const val DEFAULT_SERVICE_ACCOUNT_ICON_URL: String =
            "https://raw.githubusercontent.com/morinoparty/moripa-assets/refs/heads/main/logo/logo-72.png"
    }
}
