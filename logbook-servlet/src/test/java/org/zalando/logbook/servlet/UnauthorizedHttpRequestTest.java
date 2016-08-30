package org.zalando.logbook.servlet;

import org.junit.Test;
import org.zalando.logbook.HttpRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static java.util.Collections.emptyEnumeration;
import static org.hamcrest.Matchers.emptyString;
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
        assertThat(unit.getBody().length, is(0));
    }

    @Test
    public void shouldRemoveBodyAsString() throws IOException {
        assertThat(unit.getBodyAsString(), is(emptyString()));
    }
    
}