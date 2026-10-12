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
 * 報告の種類ごとの Embed の色 (0xRRGGBB)
 */
object ReportColor {
    /** 土地保護の削除 */
    const val CLAIM_DELETION: Int = 0x2ECC71

    /** BAN */
    const val BAN: Int = 0xE74C3C

    /** ミュート */
    const val MUTE: Int = 0x9B59B6

    /** 警告 */
    const val WARN: Int = 0xA3E635

    /** キック */
    const val KICK: Int = 0xF1A33C

    /** 処罰の解除 (恩赦) */
    const val PARDON: Int = 0x38BDF8
}
