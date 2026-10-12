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
 * MoripaActivityReport 全体の設定 (プラグインのデータフォルダにある config.json に対応する)
 *
 * Paper / Velocity で同じ形を使い、それぞれのサーバーで報告先を設定する。
 *
 * @property webhook 報告を送る Discord Webhook の設定
 */
@Serializable
data class ActivityReportConfig(
    val webhook: WebhookConfig = WebhookConfig(),
)
