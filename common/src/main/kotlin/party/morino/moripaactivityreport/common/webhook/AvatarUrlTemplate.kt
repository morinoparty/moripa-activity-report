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

import java.util.UUID

/**
 * アイコン画像 URL のテンプレートにプレイヤーの情報を埋め込む
 */
object AvatarUrlTemplate {
    /**
     * テンプレートの {name} と {uuid} を置き換える (外部状態に依存しない純粋関数)
     *
     * @param template アイコン画像 URL のテンプレート
     * @param name プレイヤー名
     * @param uuid プレイヤーの UUID
     * @return アイコン画像の URL。テンプレートが空の場合や、{uuid} を使うのに UUID が無い場合は null (アイコンを表示しない)
     */
    fun resolve(template: String, name: String, uuid: UUID?): String? {
        if (template.isBlank()) return null
        // UUID の無い相手 (IP アドレスなど) に壊れた URL を作らない
        if (uuid == null && template.contains("{uuid}")) return null
        return template
            .replace("{name}", name)
            .replace("{uuid}", uuid?.toString().orEmpty())
    }
}
