package org.zalando.logbook;

import java.io.IOException;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.zalando.logbook.Origin.LOCAL;
import static org.zalando.logbook.Origin.REMOTE;

interface MockHttpMessageTester {

    default void verifyRequest(final HttpRequest unit) throws IOException {
        assertThat(unit.getProtocolVersion()).isEqualTo("HTTP/1.1");
        assertThat(unit.getOrigin()).isEqualTo(REMOTE);
        assertThat(unit.getRemote()).isEqualTo("127.0.0.1");
        assertThat(unit.getMethod()).isEqualTo("GET");
        assertThat(unit.getRequestUri()).isEqualTo("http://localhost/");
        assertThat(unit.getScheme()).isEqualTo("http");
        assertThat(unit.getHost()).isEqualTo("localhost");
        assertThat(unit.getPort()).isEqualTo(Optional.of(80));
        assertThat(unit.getPath()).isEqualTo("/");
        assertThat(unit.getQuery()).isEmpty();
        assertThat(unit.getProtocolVersion()).isEqualTo("HTTP/1.1");
        assertThat(unit.getHeaders().values()).isEmpty();
        assertThat(unit.getContentType()).isEqualTo("text/plain");
        assertThat(unit.getCharset()).isEqualTo(UTF_8);
    }

    default void verifyResponse(final HttpResponse unit) throws IOException {
        assertThat(unit.getProtocolVersion()).isEqualTo("HTTP/1.1");
        assertThat(unit.getOrigin()).isEqualTo(LOCAL);
        assertThat(unit.getStatus()).isEqualTo(200);
        assertThat(unit.getHeaders().values()).isEmpty();
        assertThat(unit.getContentType()).isEqualTo("text/plain");
        assertThat(unit.getCharset()).isEqualTo(UTF_8);
    }

    default <S, T> void assertWith(final S unit, final BiFunction<? super S, ? super T, S> with,
            final T value, final Function<? super S, T> getter) {

        final T before = getter.apply(unit);

        assertThat(unit).isSameAs(with.apply(unit, before));
        assertThat(value).isNotEqualTo(before);

        final S applied = with.apply(unit, value);
        assertThat(unit).isNotSameAs(applied);

        final T after = getter.apply(applied);
        assertThat(after).isEqualTo(value);
    }

}
