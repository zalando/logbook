package org.zalando.logbook;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.zalando.logbook.DefaultLogbook.SimpleCorrelation;
import org.zalando.logbook.DefaultLogbook.SimplePrecorrelation;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static java.time.Duration.ZERO;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class DefaultHttpLogWriterLevelTest {

    static Iterable<Arguments> data() {
        final Logger logger = mock(Logger.class);

        return Arrays.asList(
                Arguments.of(create(logger, Level.TRACE), logger, activator(Logger::isTraceEnabled), consumer(Logger::trace)),
                Arguments.of(create(logger, Level.DEBUG), logger, activator(Logger::isDebugEnabled), consumer(Logger::debug)),
                Arguments.of(create(logger, Level.INFO), logger, activator(Logger::isInfoEnabled), consumer(Logger::info)),
                Arguments.of(create(logger, Level.WARN), logger, activator(Logger::isWarnEnabled), consumer(Logger::warn)),
                Arguments.of(create(logger, Level.ERROR), logger, activator(Logger::isErrorEnabled), consumer(Logger::error))
        );
    }

    private static DefaultHttpLogWriter create(final Logger logger, final Level trace) {
        return new DefaultHttpLogWriter(logger, trace);
    }

    private static Predicate<Logger> activator(final Predicate<Logger> predicate) {
        return predicate;
    }

    private static BiConsumer<Logger, String> consumer(final BiConsumer<Logger, String> consumer) {
        return consumer;
    }

    @ParameterizedTest
    @MethodSource("data")
    void shouldBeEnabled(final HttpLogWriter unit, final Logger logger, final Predicate<Logger> isEnabled)
            throws IOException {
        unit.isActive(mock(RawHttpRequest.class));

        isEnabled.test(verify(logger));
    }

    @ParameterizedTest
    @MethodSource("data")
    void shouldLogRequestWithCorrectLevel(final HttpLogWriter unit, final Logger logger,
            @SuppressWarnings("unused") final Predicate<Logger> isEnabled, final BiConsumer<Logger, String> log)
            throws IOException {
        unit.writeRequest(new SimplePrecorrelation<>("1", "foo", MockHttpRequest.create()));

        log.accept(verify(logger), "foo");
    }

    @ParameterizedTest
    @MethodSource("data")
    void shouldLogResponseWithCorrectLevel(final HttpLogWriter unit, final Logger logger,
            @SuppressWarnings("unused") final Predicate<Logger> isEnabled, final BiConsumer<Logger, String> log)
            throws IOException {
        unit.writeResponse(new SimpleCorrelation<>("1", ZERO, "foo", "bar",
                MockHttpRequest.create(), MockHttpResponse.create()));

        log.accept(verify(logger), "bar");
    }

}
