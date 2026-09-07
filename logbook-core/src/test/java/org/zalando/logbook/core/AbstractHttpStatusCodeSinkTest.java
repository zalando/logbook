package org.zalando.logbook.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.spi.LoggingEventBuilder;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AbstractHttpStatusCodeSinkTest {

    final Logger logger = mock(Logger.class);
    final LoggingEventBuilder loggingEventBuilder = mock(LoggingEventBuilder.class);
    final AbstractHttpStatusCodeSink unit = spy(new AbstractHttpStatusCodeSink(logger) {
        @Override
        public void write(Precorrelation precorrelation, HttpRequest request) {
            // For testing
        }

        @Override
        public void write(Correlation correlation, HttpRequest request, HttpResponse response) {
            // For testing
        }
    });

    @ParameterizedTest
    @ValueSource(ints = {100, 200, 300, 399})
    void shouldProvideLoggingEventBuilderAtTraceForResponseCodes1To399(int status) {
        when(logger.atTrace()).thenReturn(loggingEventBuilder);

        LoggingEventBuilder result = unit.getLoggingEventBuilder(status);

        verify(logger).atTrace();
        assertThat(result).isEqualTo(loggingEventBuilder);
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 499})
    void shouldProvideLoggingEventBuilderAtWarnForResponseCode4xxTo499(int status) {
        when(logger.atWarn()).thenReturn(loggingEventBuilder);

        LoggingEventBuilder result = unit.getLoggingEventBuilder(status);

        verify(logger).atWarn();
        assertThat(result).isEqualTo(loggingEventBuilder);
    }

    @ParameterizedTest
    @ValueSource(ints = {500, 599})
    void shouldProvideLoggingEventBuilderAtErrorForResponseCodes5xxAndHigher(int status) {
        when(logger.atError()).thenReturn(loggingEventBuilder);

        LoggingEventBuilder result = unit.getLoggingEventBuilder(status);

        verify(logger).atError();
        assertThat(result).isEqualTo(loggingEventBuilder);
    }

    @Test
    void shouldBeInactiveIfAllTraceWarnErrorLevelsDisabled() {
        when(logger.isTraceEnabled()).thenReturn(false);
        when(logger.isWarnEnabled()).thenReturn(false);
        when(logger.isErrorEnabled()).thenReturn(false);

        assertThat(unit.isActive()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("activeLoggerLevelConfigurations")
    void shouldBeActiveIfAnyOfTraceWarnErrorLevelsEnabled(boolean traceEnabled, boolean warnEnabled, boolean errorEnabled) {
        when(logger.isTraceEnabled()).thenReturn(traceEnabled);
        when(logger.isWarnEnabled()).thenReturn(warnEnabled);
        when(logger.isErrorEnabled()).thenReturn(errorEnabled);

        assertThat(unit.isActive()).isTrue();
    }

    static Stream<Arguments> activeLoggerLevelConfigurations() {
        return Stream.of(
                Arguments.of(true, false, false),
                Arguments.of(false, true, false),
                Arguments.of(false, false, true),
                Arguments.of(true, false, true),
                Arguments.of(false, false, true),
                Arguments.of(true, true, false),
                Arguments.of(true, true, true)
        );
    }
}