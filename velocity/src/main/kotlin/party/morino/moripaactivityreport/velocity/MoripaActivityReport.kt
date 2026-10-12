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
package party.morino.moripaactivityreport.velocity

import com.google.inject.Inject
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import kotlinx.serialization.SerializationException
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.velocity.VelocityCommandManager
import org.koin.core.context.GlobalContext
import org.koin.core.context.GlobalContext.getOrNull
import org.koin.dsl.module
import org.slf4j.Logger
import party.morino.moripaactivityreport.common.MoripaActivityReportCommon
import party.morino.moripaactivityreport.common.config.ActivityReportConfigLoader
import party.morino.moripaactivityreport.common.webhook.ActivityReporter
import party.morino.moripaactivityreport.velocity.commands.ReloadCommand
import party.morino.moripaactivityreport.velocity.punishment.LibertyBansReporter
import java.io.IOException
import java.nio.file.Path

/**
 * MoripaActivityReport の Velocity 向けプラグイン本体
 *
 * LibertyBans が導入されている場合に、処罰と処罰の解除を Discord に報告する。
 * プラグインのメタデータ (id / version など) は velocity/build.gradle.kts の velocityPluginJson で生成する。
 */
class MoripaActivityReport @Inject constructor(
    private val server: ProxyServer,
    private val logger: Logger,
    // config.json を配置するプラグイン専用のデータフォルダ
    @DataDirectory private val dataDirectory: Path,
) {
    /** 報告の送信窓口 (設定の再読み込みでは作り直さず、送信先だけを差し替える) */
    private val reporter = ActivityReporter(java.util.logging.Logger.getLogger(JUL_LOGGER_NAME))

    /** LibertyBans の処罰の報告 (LibertyBans が導入されていない場合は null) */
    private var libertyBansReporter: LibertyBansReporter? = null

    @Subscribe
    @Suppress("UnusedParameter")
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        setupKoin()
        MoripaActivityReportCommon.init()
        reloadSettings().onFailure { e -> logger.error("Failed to load {}: {}", ActivityReportConfigLoader.CONFIG_FILE_NAME, e.message) }

        val commandManager = VelocityCommandManager<CommandSource>(
            server.pluginManager.ensurePluginContainer(this),
            server,
            ExecutionCoordinator.asyncCoordinator(),
            SenderMapper.identity(),
        )
        ReloadCommand(commandManager).register()

        // LibertyBans は任意依存。API のクラスに触れる前に、導入されているかを Velocity の API だけで確認する
        if (server.pluginManager.getPlugin(LIBERTY_BANS_PLUGIN_ID).isPresent) {
            libertyBansReporter = LibertyBansReporter(reporter, java.util.logging.Logger.getLogger(JUL_LOGGER_NAME)).also { it.start() }
            logger.info("LibertyBans is installed; punishments are reported to Discord")
        } else {
            logger.info("LibertyBans is not installed; punishment reports are disabled")
        }

        logger.info("MoripaActivityReport has been enabled!")
    }

    @Subscribe
    @Suppress("UnusedParameter")
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        libertyBansReporter?.stop()
        libertyBansReporter = null
        logger.info("MoripaActivityReport has been disabled!")
    }

    /**
     * config.json を読み込み直し、報告の送信先に反映する (/marv reload)
     *
     * @return 成功した場合は Unit、設定が読み込めなかった場合はその例外 (送信先は変更しない)
     */
    fun reloadSettings(): Result<Unit> = try {
        val config = ActivityReportConfigLoader(dataDirectory).load()
        reporter.apply(config.webhook)
        Result.success(Unit)
    } catch (e: SerializationException) {
        // JSON の構文や値の誤り
        Result.failure(e)
    } catch (e: IllegalArgumentException) {
        // kotlinx-serialization は一部の誤りを IllegalArgumentException として投げる
        Result.failure(e)
    } catch (e: IOException) {
        // ファイルの読み書きの失敗
        Result.failure(e)
    }

    /**
     * Koin DI コンテナの初期化
     */
    private fun setupKoin() {
        if (getOrNull() != null) {
            return
        }

        val appModule = module {
            single<ProxyServer> { server }
            single<Logger> { this@MoripaActivityReport.logger }
            single<MoripaActivityReport> { this@MoripaActivityReport }
            single { reporter }
        }

        getOrNull() ?: GlobalContext.startKoin {
            modules(appModule)
        }
    }

    companion object {
        /** LibertyBans のプラグイン ID */
        private const val LIBERTY_BANS_PLUGIN_ID = "libertybans"

        /** 報告の失敗を記録する java.util.logging のロガーの名前 */
        private const val JUL_LOGGER_NAME = "MoripaActivityReport"
    }
}
