package org.zalando.logbook.okhttp;

import okhttp3.Request;
import org.junit.jupiter.api.Test;

import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;

final class LocalRequestTest {

    private LocalRequest unit(final Request request) {
        return new LocalRequest(request);
    }

    @Test
    void shouldResolveLocalhost() {
        final LocalRequest unit = unit(get("http://localhost/"));

        assertThat(unit.getRemote()).isEqualTo("localhost");
    }

    @Test
    void shouldIgnoreDefaultHttpPort() {
        final LocalRequest unit = unit(get("http://localhost/"));

        assertThat(unit.getPort()).isEmpty();
    }

    @Test
    void shouldIgnoreDefaultHttpsPort() {
        final LocalRequest unit = unit(get("https://localhost/"));

        assertThat(unit.getPort()).isEmpty();
    }

    private Request get(final String uri) {
        return new Request.Builder()
                .url(uri)
                .get()
                .build();
    }

}
