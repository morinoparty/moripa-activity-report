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
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.resource.factory)
}

group = "party.morino"
version = project.version.toString()

dependencies {
    implementation(project(":common"))
    implementation(project(":api"))
    compileOnly(libs.paper.api)

    implementation(libs.bundles.commands.paper)
    // cloud-kotlin-coroutines が推移的に持ち込む kotlin-reflect は古い版 (2.0.x) になるため、stdlib と同じ版にそろえる
    implementation(libs.kotlin.reflect)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.bundles.coroutines.bukkit)

    // JARにバンドル
    implementation(libs.koin.core)

    // テスト依存関係
    testImplementation(libs.paper.api)
    testImplementation(libs.bundles.junit.jupiter)
    testImplementation(libs.bundles.koin.test)
    testImplementation(libs.mockk)
    testImplementation(libs.mock.bukkit)
}

// JAR に同梱する依存。これ以外の runtimeClasspath の外部依存は MoripaActivityReportLoader が実行時に Paper に取得させる
fun isBundled(
    group: String,
    version: String,
): Boolean =
    // common / api モジュール
    group == "party.morino" ||
        // スナップショット版 (cloud など) は Paper が参照する Maven Central のミラーにない
        version.endsWith("-SNAPSHOT")

// runtimeClasspath のうち同梱しない外部依存 (KMP は解決済みの -jvm アーティファクトになる)
val runtimeLibraries =
    configurations.runtimeClasspath.map { configuration ->
        configuration.incoming.artifacts.artifacts
            .mapNotNull { it.id.componentIdentifier as? ModuleComponentIdentifier }
            .filterNot { isBundled(it.group, it.version) }
            .map { "${it.group}:${it.module}:${it.version}" }
            .distinct()
    }

// MoripaActivityReportLoader が読み込むライブラリ一覧をリソースとして生成する
val generatePaperLibraries by tasks.registering {
    val libraries = runtimeLibraries
    val outputDirectory = layout.buildDirectory.dir("generated/paper-libraries")
    // 依存が変わったときだけ再生成されるようにする
    inputs.property("libraries", libraries)
    outputs.dir(outputDirectory)
    doLast {
        outputDirectory.get().file("paper-libraries.txt").asFile.writeText(libraries.get().joinToString("\n"))
    }
}

sourceSets.main {
    resources.srcDir(generatePaperLibraries)
}

tasks {
    build {
        dependsOn("shadowJar")
    }
    shadowJar {
        // Paper が実行時に取得するので同梱しない
        dependencies {
            exclude { !isBundled(it.moduleGroup, it.moduleVersion) }
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
    runServer {
        minecraftVersion("26.2")
    }
}

sourceSets.main {
    resourceFactory {
        paperPluginYaml {
            name = rootProject.name
            version = project.version.toString()
            website = "https://github.com/morinoparty/moripa-activity-report"
            main = "$group.moripaactivityreport.paper.MoripaActivityReport"
            bootstrapper = "$group.moripaactivityreport.paper.MoripaActivityReportBootstrap"
            loader = "$group.moripaactivityreport.paper.MoripaActivityReportLoader"
            apiVersion = "26.2"
        }
    }
}
