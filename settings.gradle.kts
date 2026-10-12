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

rootProject.name = "MoripaActivityReport"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://papermc.io/repo/repository/maven-public/")
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// ビルドキャッシュはデフォルトの保存先 (GRADLE_USER_HOME/caches/build-cache-1) を使う。
// CI の gradle/actions/setup-gradle は GRADLE_USER_HOME しか保存しないため、
// プロジェクト配下に置くとジョブ間でキャッシュが引き継がれず、毎回フルコンパイルになる
buildCache {
    local {
        isEnabled = true
    }
}
include("common")
include("paper")
include("velocity")
include("api")
