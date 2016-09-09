package org.zalando.logbook.servlet;

import org.junit.Test;
import org.zalando.logbook.HttpRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyEnumeration;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class UnauthorizedHttpRequestTest {

    private final HttpRequest unit;

    public UnauthorizedHttpRequestTest() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeaderNames()).thenReturn(emptyEnumeration());
        this.unit = new UnauthorizedHttpRequest(new RemoteRequest(request));
    }

    @Test
    public void shouldRemoveBody() throws IOException {
        assertThat(new String(unit.getBody(), UTF_8), is("<skipped>"));
    }

    @Test
    public void shouldRemoveBodyAsString() throws IOException {
        assertThat(unit.getBodyAsString(), is("<skipped>"));
    }
    
}