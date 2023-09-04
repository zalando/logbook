package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.zalando.logbook.BodyFilter;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.test.MockHttpRequest;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@LogbookTest(properties = "logbook.format.style = http")
class ObfuscateBodyCustomTest {

    @Autowired
    private Logbook logbook;

    @MockBean
    private HttpLogWriter writer;

    @MockBean
    @Qualifier("bodyFilter")
    private BodyFilter bodyFilter;

    @BeforeEach
    void setUp() {
        doReturn(true).when(writer).isActive();
        doReturn("<secret>").when(bodyFilter).filter(anyString(), anyString());
    }

    @Test
    void shouldFilterRequestBody() throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withBodyAsString("Hello");

        logbook.process(request).write();

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Precorrelation.class), captor.capture());
        final String message = captor.getValue();

        assertThat(message)
                .doesNotContain("Hello")
                .contains("<secret>");
    }

}
