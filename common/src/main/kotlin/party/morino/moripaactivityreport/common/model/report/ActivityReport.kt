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

/**
 * Discord Webhook に送る報告 1 件分
 *
 * 実行者は Webhook の送信者 (名前とアイコン) として、対象者は Embed の上部 (author) として表示する。
 *
 * @property executor 操作した人 (コンソールなどプレイヤー以外の場合は null で、サービスアカウントとして表示する)
 * @property target 操作された人
 * @property color Embed の左端の色 (0xRRGGBB)
 * @property fields Embed に並べる項目
 */
data class ActivityReport(
    val executor: ReportActor?,
    val target: ReportActor,
    val color: Int,
    val fields: List<ReportField>,
)
