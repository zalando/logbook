package org.zalando.logbook.servlet;

import org.junit.Test;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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

        final LocalResponse unit = new LocalResponse(mock, "1");
        unit.getOutputStream().write("test".getBytes());

        final byte[] body1 = unit.getBody();
        final byte[] body2 = unit.getBody();

        assertSame(body1, body2);
    }

    @Test
    public void shouldDelegateGetOutputStream() throws IOException {
        final HttpServletResponse mock = mock(HttpServletResponse.class);

        final LocalResponse unit = new LocalResponse(mock, "1");
        unit.withoutBody();

        unit.getOutputStream();
        unit.getOutputStream();

        verify(mock, times(2)).getOutputStream();
        verify(mock).getCharacterEncoding();
        verifyNoMoreInteractions(mock);
    }

    @Test
    public void shouldDelegateGetWriter() throws IOException {
        final HttpServletResponse mock = mock(HttpServletResponse.class);

        final LocalResponse unit = new LocalResponse(mock, "1");
        unit.withoutBody();

        unit.getWriter();
        unit.getWriter();

        verify(mock, times(2)).getWriter();
        verify(mock).getCharacterEncoding();
        verifyNoMoreInteractions(mock);
    }

}
