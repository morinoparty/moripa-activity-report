/*
 * Written in 2026 by Nikomaru <nikomaru@nikomaru.dev>
 *
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide.This software is distributed without any warranty.
 *
 * You should have received a copy of the CC0 Public Domain Dedication along with this software.
 * If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
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

// JAR に同梱する依存。これ以外の runtimeClasspath の外部依存は PluginNameLoader が実行時に Paper に取得させる
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

// PluginNameLoader が読み込むライブラリ一覧をリソースとして生成する
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
            website = "https://github.com/morinoparty/PluginName"
            main = "$group.pluginname.paper.PluginName"
            bootstrapper = "$group.pluginname.paper.PluginNameBootstrap"
            loader = "$group.pluginname.paper.PluginNameLoader"
            apiVersion = "26.2"
        }
    }
}
