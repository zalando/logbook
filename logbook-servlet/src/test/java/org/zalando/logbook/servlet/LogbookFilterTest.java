package org.zalando.logbook.servlet;

import io.undertow.servlet.util.EmptyEnumeration;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.Logbook;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

final class LogbookFilterTest {

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
        Logbook.ResponseWritingStage responseWritingStage = mock(Logbook.ResponseWritingStage    .class);
        LogbookFilter filter = new LogbookFilter(logbook);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(logbook.process(any())).thenReturn(requestWritingStage);
        when(requestWritingStage.write()).thenReturn(requestWritingStage);
        when(requestWritingStage.process(any())).thenReturn(responseWritingStage);
        when(request.getHeaderNames()).thenReturn(EmptyEnumeration.instance());
        when(request.getDispatcherType()).thenReturn(DispatcherType.REQUEST);
        when(request.getAttribute(any())).thenReturn(new AtomicBoolean(false));

        doThrow(new IOException("Simulated IOException")).when(response).flushBuffer();

        filter.doFilter(request, response, chain);

        verify(responseWritingStage).write();
    }

}
