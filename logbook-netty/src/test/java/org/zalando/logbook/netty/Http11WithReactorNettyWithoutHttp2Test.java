package org.zalando.logbook.netty;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

final class Http11WithReactorNettyWithoutHttp2Test {

    @Test
    void shouldInstallRegistrarWithoutHttp2Codec() throws IOException, InterruptedException {
        final List<String> classPath = Http11WithoutHttp2Test.withoutArtifacts("netty-codec-http2-");

        assertThat(classPath)
                .noneMatch(entry -> Http11WithoutHttp2Test.artifactName(entry).startsWith("netty-codec-http2-"));
        assertThat(classPath)
                .anyMatch(entry -> Http11WithoutHttp2Test.artifactName(entry).startsWith("reactor-netty-"));

        Http11WithoutHttp2Test.assertSuccessfulSubprocess(
                classPath, Http11WithReactorNettyWithoutHttp2Main.class.getName());
    }
}
