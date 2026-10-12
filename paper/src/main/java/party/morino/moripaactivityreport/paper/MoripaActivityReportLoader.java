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
package party.morino.moripaactivityreport.paper;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * MoripaActivityReport のローダークラス。
 * ビルド時に生成したライブラリ一覧 (JAR に同梱していない依存) を Paper に取得させる。
 *
 * Kotlin 標準ライブラリ自体もここで取得するため、このクラスは Kotlin ではなく Java で記述する。
 */
@SuppressWarnings("unused")
public class MoripaActivityReportLoader implements PluginLoader {
    // paper/build.gradle.kts の generatePaperLibraries タスクが生成するリソース
    private static final String LIBRARIES_RESOURCE = "paper-libraries.txt";

    // Paper が推奨する Maven Central のミラー (Maven Central を直接参照すると Paper に警告される)
    private static final String MAVEN_CENTRAL_MIRROR = "https://maven-central.storage-download.googleapis.com/maven2";

    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(LIBRARIES_RESOURCE)) {
            if (stream == null) {
                throw new IllegalStateException(LIBRARIES_RESOURCE + " is not found in the plugin jar");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            // 1 行に 1 つの Maven 座標 (group:artifact:version) が書かれている
            reader.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .forEach(coordinate -> resolver.addDependency(new Dependency(new DefaultArtifact(coordinate), null)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        resolver.addRepository(new RemoteRepository.Builder("central", "default", MAVEN_CENTRAL_MIRROR).build());

        classpathBuilder.addLibrary(resolver);
    }
}
