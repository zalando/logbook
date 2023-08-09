package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
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


@LogbookTest(profiles = "truncation_order")
class BodyTruncationOrderedTest {

    @Autowired
    private Logbook logbook;

    @MockBean
    private HttpLogWriter writer;

    @BeforeEach
    void setUp() {
        doReturn(true).when(writer).isActive();
    }

    @Test
    void shouldTruncateAfterObfuscation() throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withBodyAsString("{ \"first_name\": \"Jonny\", \"details\": { \"last_name\": \"Pepp\", \"field\":\"value\" } }")
                .withContentType(MediaType.APPLICATION_JSON_VALUE);

        logbook.process(request).write();

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Precorrelation.class), captor.capture());
        final String message = captor.getValue();

        Pattern pattern = Pattern.compile(".*body\":(.*)");
        Matcher matcher = pattern.matcher(message);
        String body = null;
        if (matcher.find()) {
            body = matcher.group(1);
        }
        assertThat(body).isEqualTo("{\"first_name\":\"XXX\",...}");
    }
}
