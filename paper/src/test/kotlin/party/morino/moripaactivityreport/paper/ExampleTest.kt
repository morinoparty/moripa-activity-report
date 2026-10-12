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

package party.morino.moripaactivityreport.paper

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockbukkit.mockbukkit.ServerMock
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExtendWith(MoripaActivityReportTest::class)
class ExampleTest : KoinTest {

    private val server: ServerMock by inject()

    @Test
    @DisplayName("Plugin is enabled successfully")
    fun pluginIsEnabled() {
        val plugin = MoripaActivityReportTest.plugin
        assertNotNull(plugin)
        assertTrue(plugin.isEnabled)
    }

    @Test
    @DisplayName("Server has the plugin loaded")
    fun serverHasPlugin() {
        val pluginManager = server.pluginManager
        val plugin = pluginManager.getPlugin("MoripaActivityReport")
        assertNotNull(plugin)
    }
}
