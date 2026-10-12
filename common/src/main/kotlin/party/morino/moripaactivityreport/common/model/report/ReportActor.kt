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
package party.morino.moripaactivityreport.common.model.report

import java.util.UUID

/**
 * 報告に登場する人 (操作した人 / 操作された人)
 *
 * @property name 表示名
 * @property uuid プレイヤーの UUID (IP アドレスなどプレイヤー以外の場合は null で、アイコンを表示しない)
 */
data class ReportActor(
    val name: String,
    val uuid: UUID?,
)
