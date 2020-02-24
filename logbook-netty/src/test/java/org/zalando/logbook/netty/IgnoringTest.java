package org.zalando.logbook.netty;

import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static java.nio.charset.StandardCharsets.UTF_8;

final class IgnoringTest {

    final AtomicReference<State> state =
            new AtomicReference<>(new Unbuffered());

    final HttpRequest request = new DefaultHttpRequest(HTTP_1_1, GET, "/");

    @Test
    void shouldKeepBufferingWhenIgnoring() {
        state.updateAndGet(State::with);
        state.updateAndGet(state -> state.buffer(request, content("foo")));
        state.updateAndGet(State::without);
        state.updateAndGet(state -> state.buffer(request, content("bar")));
        // this transition is almost impossible to test under normal
        // circumstances because the test would be racing with IO
        state.updateAndGet(State::with);

        final String body = new String(state.get().getBody(), UTF_8);
        Assertions.assertEquals("foobar", body);
    }

    private DefaultHttpContent content(final String s) {
        return new DefaultHttpContent(wrappedBuffer(s.getBytes(UTF_8)));
    }

}
