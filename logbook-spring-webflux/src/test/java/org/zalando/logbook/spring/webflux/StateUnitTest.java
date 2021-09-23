package org.zalando.logbook.spring.webflux;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;


public class StateUnitTest {

    @Test
    void shouldBufferWhenIgnoring() {
        AtomicReference<State> state = new AtomicReference<>(new State.Offering());
        state.updateAndGet(State::without);
        state.updateAndGet(s -> s.buffer(new byte[0]));
        state.updateAndGet(State::with);
        state.updateAndGet(s -> s.buffer(new byte[0]));
        state.updateAndGet(State::without);
        state.updateAndGet(s -> s.buffer("Hello, world!".getBytes(StandardCharsets.UTF_8)));
        assertThat(new String(state.get().getBody(), StandardCharsets.UTF_8)).isEqualTo("Hello, world!");
    }
}
