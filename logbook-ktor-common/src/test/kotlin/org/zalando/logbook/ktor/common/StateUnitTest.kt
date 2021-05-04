package org.zalando.logbook.ktor.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.zalando.logbook.common.EMPTY_BODY
import org.zalando.logbook.common.State
import java.util.concurrent.atomic.AtomicReference
import kotlin.text.Charsets.UTF_8

internal class StateUnitTest {

    @Test
    fun `Should keep buffering when ignoring`() {
        val state: AtomicReference<State> = AtomicReference(State.Offering)
        state.updateAndGet { it.without() }
        state.updateAndGet { it.with() }
        state.updateAndGet { it.buffer(EMPTY_BODY) }
        state.updateAndGet { it.without() }
        state.updateAndGet { it.buffer("foo".toByteArray()) }
        state.updateAndGet { it.with() }
        state.updateAndGet { it.with() }
        state.updateAndGet { it.without() }
        state.updateAndGet { it.with() }
        val body = state.get().body.toString(UTF_8)
        assertEquals("foo", body)
    }

    @Test
    fun `Should not buffer when unbuffered`() {
        val state: AtomicReference<State> = AtomicReference(State.Unbuffered)
        state.updateAndGet { it.with() }
        state.updateAndGet { it.without() }
        state.updateAndGet { it.buffer("foo".toByteArray()) }
        state.updateAndGet { it.without() }
        val body = state.get().body
        assertEquals(EMPTY_BODY, body)
    }
}