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

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.GlobalContext.getOrNull
import org.koin.dsl.module
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock

/**
 * テスト用の JUnit 5 拡張クラス
 * MockBukkit と Koin の初期化・クリーンアップを行う
 */
class MoripaActivityReportTest :
    BeforeAllCallback,
    AfterAllCallback {

    companion object {
        lateinit var server: ServerMock
        lateinit var plugin: MoripaActivityReport
    }

    override fun beforeAll(context: ExtensionContext) {
        // MockBukkit サーバーを初期化
        server = MockBukkit.mock()

        // テスト用の Koin モジュールを設定
        val appModule = module {
            single<ServerMock> { server }
        }

        // Koin が未初期化の場合のみ開始
        getOrNull() ?: GlobalContext.startKoin {
            modules(appModule)
        }

        // プラグインをロード
        plugin = MockBukkit.load(MoripaActivityReport::class.java)

        // プラグインインスタンスを Koin に登録
        GlobalContext.get().declare(plugin)
    }

    override fun afterAll(context: ExtensionContext) {
        MockBukkit.unmock()
        GlobalContext.stopKoin()
    }
}
