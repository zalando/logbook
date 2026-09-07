package org.zalando.logbook.netty;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

final class Http11WithoutHttp2Test {

    @Test
    void shouldLogHttp11WithoutHttp2() throws IOException, InterruptedException {
        final List<String> classPath = withoutArtifacts("netty-codec-http2-", "reactor-netty");

        assertThat(classPath)
                .noneMatch(entry -> artifactName(entry).startsWith("netty-codec-http2-"));
        assertThat(classPath)
                .noneMatch(entry -> artifactName(entry).equals("reactor-netty")
                        || artifactName(entry).startsWith("reactor-netty-"));

        assertSuccessfulSubprocess(classPath, Http11WithoutHttp2Main.class.getName());
    }

    static List<String> withoutArtifacts(final String... names) {
        return Arrays.stream(System.getProperty("surefire.test.class.path").split(
                        System.getProperty("path.separator")))
                .filter(entry -> Arrays.stream(names).noneMatch(name -> artifactName(entry).equals(name)
                        || artifactName(entry).startsWith(name)))
                .collect(Collectors.toList());
    }

    static void assertSuccessfulSubprocess(final List<String> classPath, final String mainClass)
            throws IOException, InterruptedException {
        final Process process = new ProcessBuilder(
                Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp",
                String.join(System.getProperty("path.separator"), classPath),
                mainClass)
                .redirectErrorStream(true)
                .start();
        final AtomicReference<String> output = new AtomicReference<>();
        final AtomicReference<IOException> outputFailure = new AtomicReference<>();
        final Thread outputReader = new Thread(() -> {
            try {
                output.set(new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
            } catch (final IOException exception) {
                outputFailure.set(exception);
            }
        });
        outputReader.start();
        try {
            assertThat(process.waitFor(Duration.ofSeconds(30).toMillis(), TimeUnit.MILLISECONDS)).isTrue();
            outputReader.join(TimeUnit.SECONDS.toMillis(5));
            assertThat(outputReader.isAlive()).isFalse();
            assertThat(outputFailure).hasValue(null);
            assertThat(process.exitValue()).describedAs(output.get()).isZero();
            assertThat(output.get()).contains("HTTP11_WITHOUT_HTTP2_SUCCESS");
            assertThat(output.get())
                    .doesNotContain("NoClassDefFoundError")
                    .doesNotContain("ClassNotFoundException")
                    .doesNotContain("LinkageError");
        } finally {
            if (process.isAlive()) {
                process.destroy();
                if (!process.waitFor(Duration.ofSeconds(5).toMillis(), TimeUnit.MILLISECONDS)) {
                    process.destroyForcibly();
                    process.waitFor(Duration.ofSeconds(5).toMillis(), TimeUnit.MILLISECONDS);
                }
            }
            outputReader.join(TimeUnit.SECONDS.toMillis(5));
        }
    }

    static String artifactName(final String entry) {
        return Path.of(entry).getFileName().toString();
    }
}
