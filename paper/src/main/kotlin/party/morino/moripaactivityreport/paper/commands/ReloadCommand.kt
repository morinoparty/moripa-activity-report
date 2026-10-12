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
package party.morino.moripaactivityreport.paper.commands

import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.incendo.cloud.paper.PaperCommandManager
import org.koin.core.context.GlobalContext
import party.morino.moripaactivityreport.common.config.ActivityReportConfigLoader
import party.morino.moripaactivityreport.paper.MoripaActivityReport

/**
 * /mar reload で config.json を読み込み直すコマンド
 *
 * @param commandManager コマンドを登録する Cloud のコマンドマネージャー
 */
class ReloadCommand(
    private val commandManager: PaperCommandManager<CommandSourceStack>,
) {
    /**
     * コマンドを登録する
     */
    fun register() {
        val command = commandManager.commandBuilder("mar")
            .literal("reload")
            .permission(PERMISSION)
            .handler { context ->
                val sender = context.sender().sender
                // コマンドはブートストラップ時に登録するため、プラグイン本体は実行時に取り出す
                val plugin = GlobalContext.getOrNull()?.getOrNull<MoripaActivityReport>()
                if (plugin == null) {
                    sender.sendRichMessage("<red>MoripaActivityReport が有効になっていません。")
                    return@handler
                }
                plugin.reloadSettings()
                    .onSuccess { sender.sendRichMessage("<green>${ActivityReportConfigLoader.CONFIG_FILE_NAME} を再読み込みしました。") }
                    .onFailure { e ->
                        sender.sendRichMessage(
                            "<red>${ActivityReportConfigLoader.CONFIG_FILE_NAME} を読み込めませんでした: <error>",
                            Placeholder.unparsed("error", e.message ?: e.toString()),
                        )
                    }
            }
            .build()

        commandManager.command(command)
    }

    companion object {
        /** /mar reload の権限 */
        const val PERMISSION: String = "moripaactivityreport.reload"
    }
}
