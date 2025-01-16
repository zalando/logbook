package org.zalando.logbook.autoconfigure;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.test.MockHttpRequest;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.skyscreamer.jsonassert.JSONCompare.compareJSON;

@LogbookTest(profiles = "replacement")
class ObfuscateReplacementTest {

    @Autowired
    private Logbook logbook;

    @MockitoBean
    private HttpLogWriter writer;

    @BeforeEach
    void setUp() {
        doReturn(true).when(writer).isActive();
    }

    @Test
    void shouldUseCustomerObfuscationReplacement() throws IOException, JSONException {
        final HttpRequest request = MockHttpRequest.create()
                .withBodyAsString("{ \"name\": \"Jonny\", \"details\": { \"field1\": \"value1\", \"field2\":\"value2\" } }")
                .withContentType(MediaType.APPLICATION_JSON_VALUE);

        logbook.process(request).write();

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Precorrelation.class), captor.capture());
        final String message = captor.getValue();

        String body = new JSONObject(message).getJSONObject("body").toString();
        JSONCompareResult result = compareJSON(body, "{ \"name\": \"ZZZ\", \"details\": { \"field1\": \"value1\", \"field2\":\"value2\" } }", JSONCompareMode.LENIENT);

        assertThat(result.passed()).isTrue();
    }
}
