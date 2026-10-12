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
package party.morino.moripaactivityreport.common.config

import kotlinx.serialization.json.Json
import party.morino.moripaactivityreport.common.model.config.ActivityReportConfig
import java.nio.file.Files
import java.nio.file.Path

/**
 * データフォルダの config.json を読み込む
 *
 * ファイルが無い場合は既定値を書き出してから読み込むため、初回起動後に URL を書き足せばよい。
 *
 * @param dataDirectory プラグインのデータフォルダ
 */
class ActivityReportConfigLoader(
    private val dataDirectory: Path,
) {
    /**
     * 設定を読み込む
     *
     * @return 読み込んだ設定
     * @throws kotlinx.serialization.SerializationException 内容が JSON として正しくない場合
     * @throws java.io.IOException ファイルの読み書きに失敗した場合
     */
    fun load(): ActivityReportConfig {
        val file = dataDirectory.resolve(CONFIG_FILE_NAME)
        if (Files.notExists(file)) {
            // 初回起動時は、設定できる項目が分かるよう既定値をすべて書き出す
            Files.createDirectories(dataDirectory)
            Files.writeString(file, JSON.encodeToString(ActivityReportConfig.serializer(), ActivityReportConfig()))
        }
        return JSON.decodeFromString(ActivityReportConfig.serializer(), Files.readString(file))
    }

    companion object {
        /** 設定ファイルの名前 */
        const val CONFIG_FILE_NAME: String = "config.json"

        /** 読み書きに使う JSON の形式 (人が編集しやすいよう整形し、既定値も書き出す) */
        private val JSON = Json {
            prettyPrint = true
            encodeDefaults = true
            ignoreUnknownKeys = true
        }
    }
}
