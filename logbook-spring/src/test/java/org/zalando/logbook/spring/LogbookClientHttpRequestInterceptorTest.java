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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.HttpMessage;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(MockitoJUnitRunner.class)
public final class LogbookClientHttpRequestInterceptorTest {

    @Mock
    private HttpLogFormatter formatter;

    @Mock
    private HttpLogWriter writer;

    private final RestTemplate template = new RestTemplate();

    private MockRestServiceServer server;

    @Before
    public void setUp() throws IOException {
        when(writer.isActive(any())).thenReturn(true);

        template.setInterceptors(singletonList(new LogbookClientHttpRequestInterceptor(Logbook.builder()
                .formatter(formatter)
                .writer(writer)
                .build())));

        this.server = MockRestServiceServer.createServer(template);
    }

    @Test
    public void shouldBypassIfWriterIsInactive() throws IOException {
        when(writer.isActive(any())).thenReturn(false);

        server.expect(requestTo("http://example.org"))
                .andRespond(withSuccess());

        template.getForObject("http://example.org", Object.class);

        verify(writer, never()).writeRequest(any());
        verify(writer, never()).writeResponse(any());
    }

    @Test
    public void shouldLogRequest() throws IOException {
        server.expect(requestTo("http://example.org"))
                .andRespond(withSuccess());

        template.getForObject("http://example.org", Object.class);
        final HttpRequest request = interceptRequest();

        assertThat(request, hasFeature("remote", HttpRequest::getRemote, is(notNullValue())));
        assertThat(request, hasFeature("method", HttpRequest::getMethod, is("GET")));
        assertThat(request, hasFeature("request uri", HttpRequest::getRequestUri, hasToString("http://example.org")));
        assertThat(request, hasFeature("headers", this::headers, hasEntry(equalTo("Content-Length"), hasItem("0"))));
    }

    @Test
    public void shouldLogRequestWithQueryParameters() throws IOException {
        server.expect(requestTo("http://example.org?test=true"))
                .andRespond(withSuccess());

        template.getForObject("http://example.org?test=true", Object.class);
        final HttpRequest request = interceptRequest();

        assertThat(request, hasFeature("request uri", HttpRequest::getRequestUri,
                hasToString("http://example.org?test=true")));
    }

    @Test
    public void shouldLogRequestBody() throws IOException {
        server.expect(requestTo("http://example.org"))
                .andRespond(withSuccess());

        template.postForObject("http://example.org", "Hello, world!", Object.class);
        final HttpRequest request = interceptRequest();

        assertThat(request, hasFeature("body", this::body, is("Hello, world!")));
    }

    @SuppressWarnings("unchecked")
    private HttpRequest interceptRequest() throws IOException {
        final ArgumentCaptor<Precorrelation<HttpRequest>> captor = ArgumentCaptor.forClass(Precorrelation.class);
        verify(formatter).format(captor.capture());
        return captor.getValue().getRequest();
    }

    @Test
    public void shouldLogResponse() throws IOException {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentLength(0);
        server.expect(requestTo("http://example.org"))
                .andRespond(withSuccess().headers(headers));

        template.getForObject("http://example.org", Object.class);
        final HttpResponse response = interceptResponse();

        assertThat(response, hasFeature("status", HttpResponse::getStatus, is(200)));
        assertThat(response, hasFeature("headers", this::headers, hasEntry(equalTo("Content-Length"), hasItem("0"))));
    }

    @Test
    public void shouldLogResponseBody() throws IOException {
        server.expect(requestTo("http://example.org"))
                .andRespond(withSuccess().body("Hello, world!"));

        template.getForObject("http://example.org", String.class);
        final HttpResponse response = interceptResponse();

        assertThat(response, hasFeature("body", this::body, is("Hello, world!")));
    }

    @SuppressWarnings("unchecked")
    private HttpResponse interceptResponse() throws IOException {
        final ArgumentCaptor<Correlation<HttpRequest, HttpResponse>> captor = ArgumentCaptor.forClass(Correlation.class);
        verify(formatter).format(captor.capture());
        return captor.getValue().getResponse();
    }

    private Map<String, Collection<String>> headers(final HttpMessage message) {
        return message.getHeaders().asMap();
    }

    private String body(final HttpMessage message) {
        try {
            return message.getBodyAsString();
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

}