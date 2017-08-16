package org.zalando.logbook.servlet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class LocalResponseTest {

    private HttpServletResponse mock;
    private LocalResponse unit;

    @BeforeEach
    public void setUp() throws IOException {
        mock = mock(HttpServletResponse.class);
        when(mock.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public void write(final int b) throws IOException {
            }
        });
        unit = new LocalResponse(mock, "1");
    }

    @Test
    void shouldUseSameBody() throws IOException {
        unit.getOutputStream().write("test".getBytes());

        final byte[] body1 = unit.getBody();
        final byte[] body2 = unit.getBody();

        assertSame(body1, body2);
    }

    @Test
    void shouldUseDifferentBodyAfterWrite() throws IOException {
        unit.getOutputStream().write("Hello".getBytes());
        final byte[] body1 = unit.getBody();

        unit.getOutputStream().write("World".getBytes());
        final byte[] body2 = unit.getBody();

        assertNotSame(body1, body2);
    }

    @Test
    void shouldTeeGetOutputStream() throws IOException {
        unit.withBody();

        final ServletOutputStream os1 = unit.getOutputStream();
        final ServletOutputStream os2 = unit.getOutputStream();

        assertSame(os1, os2);

        verify(mock).getOutputStream();
        verifyNoMoreInteractions(mock);
    }

    @Test
    void shouldDelegateGetOutputStream() throws IOException {
        unit.withoutBody();

        unit.getOutputStream();
        unit.getOutputStream();

        verify(mock, times(2)).getOutputStream();
        verifyNoMoreInteractions(mock);
    }

    @Test
    void shouldTeeGetWriter() throws IOException {
        unit.withBody();

        final PrintWriter writer1 = unit.getWriter();
        final PrintWriter writer2 = unit.getWriter();

        assertSame(writer1, writer2);

        verify(mock).getOutputStream();
        verify(mock).getCharacterEncoding();
        verifyNoMoreInteractions(mock);
    }

    @Test
    void shouldDelegateGetWriter() throws IOException {
        unit.withoutBody();

        unit.getWriter();
        unit.getWriter();

        verify(mock, times(2)).getWriter();
        verifyNoMoreInteractions(mock);
    }

    @Test
    void shouldNotAllowWithBodyAfterWithoutBody() throws IOException {
        assertThrows(IllegalStateException.class, () -> {
            unit.withoutBody();
            unit.withBody();
        });
    }

    @Test
    void shouldReturnNullContentTypeWhenNoContentTypeHasBeenSpecified() {
        when(mock.getContentType()).thenReturn(null);

        assertThat(unit.getContentType(), is(nullValue()));
    }
}
