package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.test.MockHttpRequest;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@LogbookTest(properties = "logbook.write.max-body-size = 20")
class WriteBodyMaxSizeTest {

    @Autowired
    private Logbook logbook;

    @MockitoBean
    private HttpLogWriter writer;

    @BeforeEach
    void setUp() {
        doReturn(true).when(writer).isActive();
    }

    @Test
    void shouldUseBodyMaxSizeFilter() throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withBodyAsString("{\"foo\":\"someLongMessage\"}")
                .withContentType(MediaType.APPLICATION_JSON_VALUE);

        logbook.process(request).write();

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Precorrelation.class), captor.capture());
        final String message = captor.getValue();

        String body = extractBody(message);

        assertThat(body).isEqualTo("{\"foo\":\"someLongMess...}");
    }

    @Test
    void shouldUseBodyMaxSizeOverDefaultFilter() throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withBodyAsString("{\"open_id\":\"someLongSecret\",\"foo\":\"bar\"}")
                .withContentType(MediaType.APPLICATION_JSON_VALUE);

        logbook.process(request).write();

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Precorrelation.class), captor.capture());
        final String message = captor.getValue();

        String body = extractBody(message);

        assertThat(body).isEqualTo("{\"open_id\":\"XXX\",\"fo...}");
    }

    private static String extractBody(String message) {
        Pattern pattern = Pattern.compile(".*body\":(.*)");
        Matcher matcher = pattern.matcher(message);
        String body = null;
        if (matcher.find()) {
            body = matcher.group(1);
        }
        return body;
    }

}
