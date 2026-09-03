package org.zalando.logbook.core;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.spi.LoggingEventBuilder;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.test.MockHttpRequest;
import org.zalando.logbook.test.MockHttpResponse;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
final class HttpStatusCodeBasedSinkTest {

    @Mock
    private HttpLogFormatter formatter;

    @Mock
    private Logger logger;

    @Mock
    private LoggingEventBuilder loggingEventBuilder;

    @InjectMocks
    private HttpStatusCodeBasedSink unit;

    @Nested
    class ActivationTests {
        static Stream<Arguments> activeLoggerLevelConfigurations() {
            return Stream.of(
                    Arguments.of(true, false, false),
                    Arguments.of(false, true, false),
                    Arguments.of(false, false, true),
                    Arguments.of(true, true, false),
                    Arguments.of(true, false, true),
                    Arguments.of(false, true, true),
                    Arguments.of(true, true, true)
            );
        }

        @ParameterizedTest
        @MethodSource("activeLoggerLevelConfigurations")
        void isActiveWhenAtLeastOneOfTraceWarnErrorLevelsActive(boolean isTraceActive, boolean isWarnActive, boolean isErrorActive) {
            lenient().when(logger.isTraceEnabled()).thenReturn(isTraceActive);
            lenient().when(logger.isWarnEnabled()).thenReturn(isWarnActive);
            lenient().when(logger.isErrorEnabled()).thenReturn(isErrorActive);

            assertThat(unit.isActive()).isTrue();
        }

        @Test
        void isInactiveWhenAllInactiveReturnsFalse() {
            when(logger.isTraceEnabled()).thenReturn(false);
            when(logger.isWarnEnabled()).thenReturn(false);
            when(logger.isErrorEnabled()).thenReturn(false);

            assertThat(unit.isActive()).isFalse();
        }
    }

    @Nested
    class WritingTests {

        @Mock
        private Precorrelation precorrelation;

        @Mock
        private Correlation correlation;

        private final MockHttpRequest mockRequest = MockHttpRequest.create();
        private static final String FORMATTED_REQUEST = "formatted-request";
        private static final String FORMATTED_RESPONSE = "formatted-response";

        @Test
        void writeRequestAtTraceLevel() throws IOException {
            when(formatter.format(precorrelation, mockRequest)).thenReturn(FORMATTED_REQUEST);
            when(logger.atTrace()).thenReturn(loggingEventBuilder);

            unit.write(precorrelation, mockRequest);

            verify(formatter).format(precorrelation, mockRequest);
            verify(logger).atTrace();
            verify(loggingEventBuilder).log(FORMATTED_REQUEST);
            verify(logger, never()).atError();
            verify(logger, never()).atWarn();
        }

        @ParameterizedTest
        @ValueSource(ints = {200, 201, 202, 299, 300, 302, 399})
        void writeResponse2xxTo3xxAtTraceLevel(int status) throws IOException {
            final MockHttpResponse response = MockHttpResponse.create().withStatus(status);
            when(formatter.format(correlation, response)).thenReturn(FORMATTED_RESPONSE);
            when(logger.atTrace()).thenReturn(loggingEventBuilder);

            unit.write(correlation, mockRequest, response);

            verify(formatter).format(correlation, response);
            verify(logger).atTrace();
            verify(loggingEventBuilder).log(FORMATTED_RESPONSE);
            verify(logger, never()).atWarn();
            verify(logger, never()).atError();
        }

        @ParameterizedTest
        @ValueSource(ints = {400, 401, 499})
        void writeResponse4xxTo499AtWarnLevel(int status) throws IOException {
            final MockHttpResponse response = MockHttpResponse.create().withStatus(status);
            when(formatter.format(correlation, response)).thenReturn(FORMATTED_RESPONSE);
            when(logger.atWarn()).thenReturn(loggingEventBuilder);

            unit.write(correlation, mockRequest, response);

            verify(formatter).format(correlation, response);
            verify(logger).atWarn();
            verify(loggingEventBuilder).log(FORMATTED_RESPONSE);
            verify(logger, never()).atTrace();
            verify(logger, never()).atError();
        }

        @ParameterizedTest
        @ValueSource(ints = {500, 501, 502, 600, 700})
        void writeResponse5xxAndHigherAtErrorLevel(int status) throws IOException {
            final MockHttpResponse response = MockHttpResponse.create().withStatus(status);
            when(formatter.format(correlation, response)).thenReturn(FORMATTED_RESPONSE);
            when(logger.atError()).thenReturn(loggingEventBuilder);

            unit.write(correlation, mockRequest, response);

            verify(formatter).format(correlation, response);
            verify(logger).atError();
            verify(loggingEventBuilder).log(FORMATTED_RESPONSE);
            verify(logger, never()).atTrace();
            verify(logger, never()).atWarn();
        }
    }
}