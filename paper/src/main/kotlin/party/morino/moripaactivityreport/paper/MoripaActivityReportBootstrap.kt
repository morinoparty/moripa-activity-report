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

import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import org.bukkit.plugin.java.JavaPlugin
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.PaperCommandManager
import party.morino.moripaactivityreport.paper.commands.ReloadCommand

@Suppress("unused", "UnstableApiUsage")
class MoripaActivityReportBootstrap : PluginBootstrap {

    override fun bootstrap(context: BootstrapContext) {
        val commandManager: PaperCommandManager<CommandSourceStack> =
            PaperCommandManager
                .builder()
                .executionCoordinator(ExecutionCoordinator.asyncCoordinator())
                .buildBootstrapped(context)

        ReloadCommand(commandManager).register()
    }

    override fun createPlugin(context: PluginProviderContext): JavaPlugin {
        return MoripaActivityReport()
    }
}
