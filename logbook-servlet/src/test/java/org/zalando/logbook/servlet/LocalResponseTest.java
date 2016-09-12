package org.zalando.logbook.servlet;

import org.junit.Test;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LocalResponseTest {

    @Test
    public void shouldUseSameBody() throws IOException {
        final HttpServletResponse mock = mock(HttpServletResponse.class);
        when(mock.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public void write(final int b) throws IOException {
            }
        });

        final LocalResponse response = new LocalResponse(mock, "1");
        response.getOutputStream().write("test".getBytes());

        final byte[] body1 = response.getBody();
        final byte[] body2 = response.getBody();

        assertSame(body1, body2);
    }
}
