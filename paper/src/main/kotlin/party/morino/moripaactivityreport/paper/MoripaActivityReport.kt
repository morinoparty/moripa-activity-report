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

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import kotlinx.serialization.SerializationException
import org.koin.core.context.GlobalContext
import org.koin.core.context.GlobalContext.getOrNull
import org.koin.dsl.module
import party.morino.moripaactivityreport.common.MoripaActivityReportCommon
import party.morino.moripaactivityreport.common.config.ActivityReportConfigLoader
import party.morino.moripaactivityreport.common.webhook.ActivityReporter
import party.morino.moripaactivityreport.paper.claim.ClaimDeletionListener
import party.morino.moripaactivityreport.paper.ticket.MoripaUtilsTicketCloser
import party.morino.moripaactivityreport.paper.ticket.TicketCloser
import party.morino.moripaactivityreport.paper.ticket.UnavailableTicketCloser
import java.io.IOException

/**
 * MoripaActivityReport の Paper 向けプラグイン本体
 *
 * GriefPrevention が導入されている場合に、/deleteclaim による土地保護の削除を Discord に報告する。
 */
open class MoripaActivityReport : SuspendingJavaPlugin() {
    /** 報告の送信窓口 (設定の再読み込みでは作り直さず、送信先だけを差し替える) */
    private val reporter: ActivityReporter by lazy { ActivityReporter(logger) }

    override suspend fun onEnableAsync() {
        setupKoin()
        MoripaActivityReportCommon.init()
        reloadSettings().onFailure { e -> logger.severe("Failed to load ${ActivityReportConfigLoader.CONFIG_FILE_NAME}: ${e.message}") }

        // GriefPrevention は任意依存。API のクラスに触れる前に、導入されているかを Bukkit の API だけで確認する
        if (server.pluginManager.getPlugin(GRIEF_PREVENTION_PLUGIN_NAME) != null) {
            server.pluginManager.registerEvents(ClaimDeletionListener(this), this)
            logger.info("GriefPrevention is installed; /deleteclaim <reason> is reported to Discord")
        } else {
            logger.info("GriefPrevention is not installed; claim deletion reports are disabled")
        }
        logger.info("${pluginMeta.name} v${pluginMeta.version} has been enabled!")
    }

    override suspend fun onDisableAsync() {
        logger.info("${pluginMeta.name} has been disabled!")
    }

    /**
     * config.json を読み込み直し、報告の送信先に反映する (/mar reload)
     *
     * @return 成功した場合は Unit、設定が読み込めなかった場合はその例外 (送信先は変更しない)
     */
    fun reloadSettings(): Result<Unit> = try {
        val config = ActivityReportConfigLoader(dataFolder.toPath()).load()
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
     * テスト環境では既に初期化済みの場合があるため、チェックを行う
     */
    private fun setupKoin() {
        if (getOrNull() != null) {
            return
        }

        val appModule = module {
            single<MoripaActivityReport> { this@MoripaActivityReport }
            single { reporter }
            single<TicketCloser> { createTicketCloser() }
        }

        getOrNull() ?: GlobalContext.startKoin {
            modules(appModule)
        }
    }

    /**
     * MoripaUtils の有無に応じて、チケットをクローズする方法を選ぶ
     *
     * @return MoripaUtils が導入されていればその公開 API を使う実装、無ければ何もしない実装
     */
    private fun createTicketCloser(): TicketCloser =
        if (server.pluginManager.getPlugin(MORIPA_UTILS_PLUGIN_NAME) != null) {
            MoripaUtilsTicketCloser(logger)
        } else {
            UnavailableTicketCloser()
        }

    companion object {
        /** GriefPrevention のプラグイン名 */
        private const val GRIEF_PREVENTION_PLUGIN_NAME = "GriefPrevention"

        /** MoripaUtils のプラグイン名 */
        private const val MORIPA_UTILS_PLUGIN_NAME = "MoripaUtils"
    }
}
