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
package party.morino.moripaactivityreport.api

interface MoripaActivityReportAPI {
    companion object {
        private var instance: MoripaActivityReportAPI? = null

        fun getInstance(): MoripaActivityReportAPI {
            return checkNotNull(instance) { "MoripaActivityReportAPI is not initialized" }
        }

        fun setInstance(api: MoripaActivityReportAPI) {
            instance = api
        }
    }
}
