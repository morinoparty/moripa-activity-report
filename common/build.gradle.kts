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
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

group = "party.morino"
version = project.version.toString()

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.velocity.api)
    implementation(libs.kotlinx.serialization.json)

    // テスト依存関係
    testImplementation(libs.bundles.junit.jupiter)
}

tasks.test {
    useJUnitPlatform()
}

// ビルド時の値 (プラグインのバージョン) を Kotlin の const val として生成する。
// Velocity の @Plugin アノテーションの引数はコンパイル時定数である必要があるため、
// Gradle 側の値を BuildConstants.kt に書き出して参照させる。
// パッケージ名を変更した場合はこの basePackage も合わせて変更すること。
val basePackage = "party.morino.moripaactivityreport"
val buildConstantsDir = layout.buildDirectory.dir("generated/sources/buildConstants/kotlin/main")

val generateBuildConstants by tasks.registering {
    val pluginVersion = project.version.toString()
    // configuration cache はスクリプトのプロパティを直列化できないため、doLast で使う値はローカル変数に移しておく
    val packageName = basePackage
    val packageDir = packageName.replace('.', '/')
    val outputDir = buildConstantsDir

    // 入力値が変わったときだけ再生成されるようにする
    inputs.property("pluginVersion", pluginVersion)
    inputs.property("basePackage", basePackage)
    outputs.dir(outputDir)

    doLast {
        val file = outputDir.get().file("$packageDir/common/BuildConstants.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            |package $packageName.common
            |
            |/**
            | * Gradle のビルド時に生成される定数
            | * このファイルは common/build.gradle.kts の generateBuildConstants タスクが生成するため、手で編集しないこと
            | */
            |object BuildConstants {
            |    /** プラグインのバージョン (gradle.properties の version) */
            |    const val VERSION = "$pluginVersion"
            |}
            |
            """.trimMargin(),
        )
    }
}

// 生成したソースをコンパイル対象に加える (タスクを渡すことで依存関係も自動的に張られる)
kotlin {
    sourceSets["main"].kotlin.srcDir(generateBuildConstants)
}
