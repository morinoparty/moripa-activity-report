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
plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    // velocity-plugin.json を Gradle で生成する (kapt によるアノテーション処理より速い)
    alias(libs.plugins.resource.factory)
    alias(libs.plugins.shadow)
}

group = "party.morino"
version = project.version.toString()

dependencies {
    implementation(project(":common"))
    implementation(project(":api"))
    compileOnly(libs.velocity.api)
    // LibertyBans の API。実行時は LibertyBans プラグイン側のクラスを使うため同梱しない
    compileOnly(libs.libertybans.api)
    // MoripaUtils の公開 API。実行時は MoripaUtils プラグイン側のクラスを使うため同梱しない
    compileOnly(libs.moripa.utils.api) { isTransitive = false }

    implementation(libs.bundles.commands.velocity)

    // Kotlin の標準ライブラリと kotlinx-serialization は JAR に同梱する
    // (他のプラグインから借りると、同名のクラスが別々のクラスローダーから読み込まれて LinkageError になるため)
    implementation(libs.kotlinx.serialization.json)
    implementation(kotlin("stdlib-jdk8"))
    compileOnly(libs.bundles.coroutines.velocity)

    // JARにバンドル
    implementation(libs.koin.core)

    // テスト依存関係
    testImplementation(libs.bundles.junit.jupiter)
    // compileOnly はテストのクラスパスに乗らないため、PunishmentType などを使うテスト用に追加する
    testImplementation(libs.libertybans.api)
    testImplementation(libs.bundles.koin.test)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.serialization.json)
    testImplementation(libs.bundles.coroutines.velocity)
    testImplementation(libs.koin.core)
    testImplementation(kotlin("stdlib-jdk8"))
}

tasks {
    build {
        dependsOn("shadowJar")
    }
    shadowJar {
        dependencies {
            // kotlin-reflect は Velocity 版のコードでは使わないため同梱しない
            exclude(dependency("org.jetbrains.kotlin:kotlin-reflect:.*"))
            exclude(dependency("org.jetbrains.kotlinx:kotlinx-coroutines-core:.*"))
            exclude(dependency("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:.*"))
            exclude(dependency("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:.*"))
            exclude(dependency("org.jetbrains.kotlinx:kotlinx-coroutines-bom:.*"))
        }
    }
    test {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }
}

sourceSets.main {
    resourceFactory {
        // @Plugin アノテーションの代わりに velocity-plugin.json を生成する
        velocityPluginJson {
            id = "moripa-activity-report"
            name = rootProject.name
            // gradle.properties の version と連動する
            version = project.version.toString()
            description = "Reports staff activities (punishments) to Discord"
            authors.add("morinoparty")
            main = "$group.moripaactivityreport.velocity.MoripaActivityReport"
            // LibertyBans は任意依存。先に読み込ませ、導入されていれば処罰を報告する
            dependency("libertybans", true)
            // MoripaUtils は任意依存。先に読み込ませ、導入されていれば処罰の理由に書かれたチケットをクローズする
            dependency("moripa-utils", true)
        }
    }
}
