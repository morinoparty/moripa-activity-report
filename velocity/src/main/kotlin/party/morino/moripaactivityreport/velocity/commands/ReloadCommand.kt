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
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.incendo.cloud.velocity.VelocityCommandManager
import org.koin.core.context.GlobalContext
import party.morino.moripaactivityreport.common.config.ActivityReportConfigLoader
import party.morino.moripaactivityreport.velocity.MoripaActivityReport

/**
 * /marv reload で config.json を読み込み直すコマンド
 *
 * @param commandManager コマンドを登録する Cloud のコマンドマネージャー
 */
class ReloadCommand(
    private val commandManager: VelocityCommandManager<CommandSource>,
) {
    /**
     * コマンドを登録する
     */
    fun register() {
        val command = commandManager.commandBuilder("marv")
            .literal("reload")
            .permission(PERMISSION)
            .handler { context ->
                val sender = context.sender()
                val plugin = GlobalContext.getOrNull()?.getOrNull<MoripaActivityReport>()
                if (plugin == null) {
                    sender.sendMessage(MINI_MESSAGE.deserialize("<red>MoripaActivityReport が有効になっていません。"))
                    return@handler
                }
                plugin.reloadSettings()
                    .onSuccess {
                        sender.sendMessage(MINI_MESSAGE.deserialize("<green>${ActivityReportConfigLoader.CONFIG_FILE_NAME} を再読み込みしました。"))
                    }
                    .onFailure { e ->
                        sender.sendMessage(
                            MINI_MESSAGE.deserialize(
                                "<red>${ActivityReportConfigLoader.CONFIG_FILE_NAME} を読み込めませんでした: <error>",
                                Placeholder.unparsed("error", e.message ?: e.toString()),
                            ),
                        )
                    }
            }
            .build()

        commandManager.command(command)
    }

    companion object {
        /** /marv reload の権限 */
        const val PERMISSION: String = "moripaactivityreport.reload"

        /** メッセージの組み立てに使う MiniMessage */
        private val MINI_MESSAGE = MiniMessage.miniMessage()
    }
}
