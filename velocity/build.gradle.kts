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

    implementation(libs.bundles.commands.velocity)

    // compileOnly
    compileOnly(libs.kotlinx.serialization.json)
    compileOnly(libs.bundles.coroutines.velocity)
    compileOnly(kotlin("stdlib-jdk8"))

    // JARにバンドル
    implementation(libs.koin.core)

    // テスト依存関係
    testImplementation(libs.bundles.junit.jupiter)
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
            exclude(dependency("org.jetbrains.kotlin:.*:.*"))
            exclude(dependency("org.jetbrains.kotlinx:kotlinx-coroutines-core:.*"))
            exclude(dependency("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:.*"))
            exclude(dependency("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:.*"))
            exclude(dependency("org.jetbrains.kotlinx:kotlinx-coroutines-bom:.*"))
            exclude(dependency("org.jetbrains.kotlinx:kotlinx-serialization-.*:.*"))
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
            id = "moripaactivityreport"
            name = rootProject.name
            // gradle.properties の version と連動する
            version = project.version.toString()
            description = "A Minecraft plugin template"
            authors.add("morinoparty")
            main = "$group.moripaactivityreport.velocity.MoripaActivityReport"
        }
    }
}
