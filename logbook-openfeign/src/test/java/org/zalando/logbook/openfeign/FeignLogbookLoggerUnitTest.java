package org.zalando.logbook.openfeign;

import feign.Logger.Level;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.core.DefaultHttpLogFormatter;
import org.zalando.logbook.core.DefaultSink;
import org.zalando.logbook.test.TestStrategy;

import java.io.IOException;
import java.util.Collections;

import static feign.Util.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class FeignLogbookLoggerUnitTest {

    @Mock
    private HttpLogWriter writer;
    private Logbook logbook;

    private FeignLogbookLogger logger;

    @BeforeEach
    void setup() {
        logbook = Logbook.builder()
                .strategy(new TestStrategy())
                .sink(new DefaultSink(new DefaultHttpLogFormatter(), writer))
                .build();
        logger = new FeignLogbookLogger(logbook);
    }

    @Test
    void responseBodyShouldBeClosedAfterRebuffer() throws IOException {
        Request request =
                Request.create(Request.HttpMethod.GET, "/api", Collections.emptyMap(), null, UTF_8, null);
        Response response = Response.builder()
                .status(200)
                .reason("OK")
                .request(request)
                .headers(Collections.emptyMap())
                .body("some text", UTF_8)
                .build();
        Response.Body spyBody = spy(response.body());
        response = response.toBuilder().body(spyBody).build();

        logger.logRequest("someMethod()", Level.FULL, request);
        Response rebufferedResponse =
                logger.logAndRebufferResponse("someMethod()", Level.FULL, response, 100);

        verify(spyBody).close();
        assertThat(rebufferedResponse.body()).isNotSameAs(spyBody);
    }
}
