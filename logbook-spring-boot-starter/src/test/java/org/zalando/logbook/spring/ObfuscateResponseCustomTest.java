package org.zalando.logbook.spring;

/*
 * #%L
 * Logbook: Spring
 * %%
 * Copyright (C) 2015 Zalando SE
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.Correlator;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.MockHttpResponse;
import org.zalando.logbook.MockRawHttpRequest;
import org.zalando.logbook.MockRawHttpResponse;
import org.zalando.logbook.ResponseObfuscator;

import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration
@TestPropertySource(properties = "logbook.format.style = http")
public final class ObfuscateResponseCustomTest extends AbstractTest {

    @Configuration
    public static class TestConfiguration {

        @Bean
        public HttpLogWriter writer() throws IOException {
            final HttpLogWriter writer = mock(HttpLogWriter.class);
            when(writer.isActive(any())).thenReturn(true);
            return writer;
        }

        @Bean
        public ResponseObfuscator responseObfuscator() {
            return request -> MockHttpResponse.response()
                    .body("<secret>")
                    .build();
        }

    }

    @Autowired
    private Logbook logbook;

    @Autowired
    private HttpLogWriter writer;

    @Test
    public void shouldObfuscateResponseBody() throws IOException {
        final Optional<Correlator> correlator = logbook.write(MockRawHttpRequest.create());

        correlator.get().write(MockRawHttpResponse.response()
                .body("Hello")
                .build());

        final ArgumentCaptor<Correlation<String, String>> captor = newCaptor();
        verify(writer).writeResponse(captor.capture());
        final String message = captor.getValue().getResponse();

        assertThat(message, not(containsString("Hello")));
        assertThat(message, containsString("<secret>"));
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<Correlation<String, String>> newCaptor() {
        return ArgumentCaptor.forClass(Correlation.class);
    }

}