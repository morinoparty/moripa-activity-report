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

package party.morino.moripaactivityreport.velocity.commands

import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import org.incendo.cloud.velocity.VelocityCommandManager

class ExampleCommand(
    private val commandManager: VelocityCommandManager<CommandSource>,
) {
    fun register() {
        val command = commandManager.commandBuilder("moripaactivityreport")
            .handler { ctx ->
                ctx.sender().sendMessage(Component.text("Hello from MoripaActivityReport!"))
            }
            .build()

        commandManager.command(command)
    }
}
