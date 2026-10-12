/*
 * Written in 2026 by Nikomaru <nikomaru@nikomaru.dev>
 *
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide.This software is distributed without any warranty.
 *
 * You should have received a copy of the CC0 Public Domain Dedication along with this software.
 * If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

rootProject.name = "PluginName"

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
