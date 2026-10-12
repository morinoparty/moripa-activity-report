/*
 * Written in 2026 by Nikomaru <nikomaru@nikomaru.dev>
 *
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide.This software is distributed without any warranty.
 *
 * You should have received a copy of the CC0 Public Domain Dedication along with this software.
 * If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package party.morino.pluginname.paper;

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
 * PluginName のローダークラス。
 * ビルド時に生成したライブラリ一覧 (JAR に同梱していない依存) を Paper に取得させる。
 *
 * Kotlin 標準ライブラリ自体もここで取得するため、このクラスは Kotlin ではなく Java で記述する。
 */
@SuppressWarnings("unused")
public class PluginNameLoader implements PluginLoader {
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
