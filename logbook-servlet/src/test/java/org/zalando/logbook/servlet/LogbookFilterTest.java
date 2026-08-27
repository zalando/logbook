package org.zalando.logbook.servlet;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockAsyncContext;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.core.DefaultHttpLogFormatter;
import org.zalando.logbook.core.DefaultSink;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Collections.emptyEnumeration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

final class LogbookFilterTest {

    private final HttpLogWriter writer = mock(HttpLogWriter.class);

    private final Logbook logbook = Logbook.builder()
            .sink(new DefaultSink(new DefaultHttpLogFormatter(), writer))
            .build();

    @BeforeEach
    void setUp() {
        when(writer.isActive()).thenReturn(true);
    }

    @Test
    void shouldCreateLogbookFilter() {
        new LogbookFilter();
    }

    @Test
    void shouldCreateSecureLogbookFilter() {
        new SecureLogbookFilter();
    }

    @Test
    void shouldCallInit() {
        new LogbookFilter().init(mock(FilterConfig.class));
    }

    @Test
    void shouldCallDestroy() {
        new LogbookFilter().destroy();
    }

    @Test
    void shouldHandleIOExceptionOnFlushBufferAndWriteResponse() throws Exception {
        Logbook logbook = mock(Logbook.class);
        Logbook.RequestWritingStage requestWritingStage = mock(Logbook.RequestWritingStage.class);
        Logbook.ResponseWritingStage responseWritingStage = mock(Logbook.ResponseWritingStage.class);
        LogbookFilter filter = new LogbookFilter(logbook);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(logbook.process(any())).thenReturn(requestWritingStage);
        when(requestWritingStage.write()).thenReturn(requestWritingStage);
        when(requestWritingStage.process(any())).thenReturn(responseWritingStage);
        when(request.getHeaderNames()).thenReturn(emptyEnumeration());
        when(request.getDispatcherType()).thenReturn(DispatcherType.REQUEST);
        when(request.getAttribute(any())).thenReturn(new AtomicBoolean(false));

        doThrow(new IOException("Simulated IOException")).when(response).flushBuffer();

        filter.doFilter(request, response, chain);

        verify(responseWritingStage).write();
    }

    @Test
    void shouldWrapAsyncOnCompleteListener() throws Exception {
        final AtomicBoolean wrapped = new AtomicBoolean();

        final MockAsyncContext asyncContext = filterAsyncRequest(
                new LogbookFilter(logbook).withAsyncOnCompleteListenerWrapper(recording(wrapped)),
                new MockHttpServletResponse());

        verify(writer, never()).write(any(Correlation.class), any());

        asyncContext.complete();

        assertThat(wrapped).isTrue();
        verify(writer).write(any(Correlation.class), any());
    }

    @Test
    void shouldWrapAsyncOnCompleteListenerInSecureFilter() throws Exception {
        final AtomicBoolean wrapped = new AtomicBoolean();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(403);

        filterAsyncRequest(new SecureLogbookFilter(logbook)
                .withAsyncOnCompleteListenerWrapper(recording(wrapped)), response).complete();

        assertThat(wrapped).isTrue();
        verify(writer).write(any(Correlation.class), any());
    }

    @Test
    void shouldNotWrapAsyncOnCompleteListenerByDefault() throws Exception {
        filterAsyncRequest(new LogbookFilter(logbook), new MockHttpServletResponse()).complete();

        verify(writer).write(any(Correlation.class), any());
    }

    private static AsyncOnCompleteListenerWrapper recording(final AtomicBoolean wrapped) {
        return listener -> event -> {
            wrapped.set(true);
            listener.onComplete(event);
        };
    }

    private static MockAsyncContext filterAsyncRequest(
            final HttpFilter filter, final MockHttpServletResponse response) throws Exception {

        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        request.setAsyncSupported(true);
        final MockAsyncContext asyncContext = (MockAsyncContext) request.startAsync();

        filter.doFilter(request, response, new MockFilterChain());

        return asyncContext;
    }

}
