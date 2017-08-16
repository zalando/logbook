package org.zalando.logbook;

import java.io.IOException;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.zalando.logbook.Origin.LOCAL;
import static org.zalando.logbook.Origin.REMOTE;

public interface MockHttpMessageTester {

    default void verifyRequest(final BaseHttpRequest unit) throws IOException {
        assertThat(unit.getProtocolVersion(), is("HTTP/1.1"));
        assertThat(unit.getOrigin(), is(REMOTE));
        assertThat(unit.getRemote(), is("127.0.0.1"));
        assertThat(unit.getMethod(), is("GET"));
        assertThat(unit.getRequestUri(), is("http://localhost/"));
        assertThat(unit.getScheme(), is("http"));
        assertThat(unit.getHost(), is("localhost"));
        assertThat(unit.getPort(), is(Optional.of(80)));
        assertThat(unit.getPath(), is("/"));
        assertThat(unit.getQuery(), is(emptyString()));
        assertThat(unit.getProtocolVersion(), is("HTTP/1.1"));
        assertThat(unit.getHeaders().values(), is(empty()));
        assertThat(unit.getContentType(), is("text/plain"));
        assertThat(unit.getCharset(), is(UTF_8));
    }

    default void verifyResponse(final BaseHttpResponse unit) throws IOException {
        assertThat(unit.getProtocolVersion(), is("HTTP/1.1"));
        assertThat(unit.getOrigin(), is(LOCAL));
        assertThat(unit.getStatus(), is(200));
        assertThat(unit.getHeaders().values(), is(empty()));
        assertThat(unit.getContentType(), is("text/plain"));
        assertThat(unit.getCharset(), is(UTF_8));
    }

    default <S, T> void assertWith(final S unit, final BiFunction<? super S, ? super T, S> with,
            final T value, final Function<? super S, T> getter) {

        final T before = getter.apply(unit);

        assertThat(unit, is(sameInstance(with.apply(unit, before))));

        assertThat(value, is(not(before)));

        final S applied = with.apply(unit, value);
        assertThat(unit, is(not(sameInstance(applied))));

        final T after = getter.apply(applied);
        assertThat(after, is(value));
    }

}
