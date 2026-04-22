package org.zalando.logbook.core;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.test.MockHttpRequest;
import org.zalando.logbook.test.MockHttpResponse;

import java.io.IOException;
import java.util.List;

import static ch.qos.logback.classic.Level.DEBUG;
import static ch.qos.logback.classic.Level.ERROR;
import static ch.qos.logback.classic.Level.TRACE;
import static ch.qos.logback.classic.Level.WARN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

final class StatusCodeBasedSinkTest {

    private final Logger logger = (Logger) LoggerFactory.getLogger(Logbook.class);

    private final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    private final List<ILoggingEvent> logsList = listAppender.list;

    {
        listAppender.start();
        logger.addAppender(listAppender);
    }

    private final HttpLogFormatter formatter = mock(HttpLogFormatter.class);
    private final StatusCodeBasedSink unit = new StatusCodeBasedSink(formatter);

    @BeforeEach
    void reset() {
        logsList.clear();
        logger.setLevel(TRACE);
    }

    @Test
    void isActive() {
        logger.setLevel(TRACE);
        assertThat(unit.isActive()).isTrue();

        logger.setLevel(DEBUG);
        assertThat(unit.isActive()).isFalse();
    }

    @Test
    void writePrecorrelationLogAtTrace() throws IOException {
        final Precorrelation precorrelation = mock(Precorrelation.class);
        final MockHttpRequest request = MockHttpRequest.create();

        when(formatter.format(precorrelation, request)).thenReturn("formatted-request");

        unit.write(precorrelation, request);

        verify(formatter).format(precorrelation, request);

        assertThat(logsList).hasSize(1);
        final ILoggingEvent event = logsList.get(0);

        assertThat(event.getMessage()).isEqualTo("formatted-request");
        assertThat(event.getLevel()).isEqualTo(TRACE);
    }

    @Test
    void writeCorrelation2xxLogsAtTrace() throws IOException {
        testWriteCorrelation(200, TRACE);
    }

    @Test
    void writeCorrelation400LogsAtWarn() throws IOException {
        testWriteCorrelation(400, WARN);
    }

    @Test
    void writeCorrelation404LogsAtWarn() throws IOException {
        testWriteCorrelation(404, WARN);
    }

    @Test
    void writeCorrelation500LogsAtError() throws IOException {
        testWriteCorrelation(500, ERROR);
    }

    @Test
    void writeCorrelation503LogsAtError() throws IOException {
        testWriteCorrelation(503, ERROR);
    }

    @Test
    void writeCorrelation399LogsAtTrace() throws IOException {
        testWriteCorrelation(399, TRACE);
    }

    private void testWriteCorrelation(final int status, final ch.qos.logback.classic.Level expectedLevel) throws IOException {
        final Correlation correlation = mock(Correlation.class);
        final MockHttpRequest request = MockHttpRequest.create();
        final MockHttpResponse response = MockHttpResponse.create().withStatus(status);

        when(formatter.format(correlation, response)).thenReturn("formatted-response-" + status);

        unit.write(correlation, request, response);

        verify(formatter).format(correlation, response);

        assertThat(logsList).hasSize(1);
        final ILoggingEvent event = logsList.get(0);

        assertThat(event.getMessage()).isEqualTo("formatted-response-" + status);
        assertThat(event.getLevel()).isEqualTo(expectedLevel);
    }

}
