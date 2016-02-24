package org.zalando.logbook.undertow;

/*
 * #%L
 * Logbook: Undertow
 * %%
 * Copyright (C) 2016 Zalando SE
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.internal.ThrowableCauseMatcher.hasCause;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.zalando.logbook.Correlator;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;

import com.google.common.base.Throwables;

import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.ServerConnection;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;

@RunWith(MockitoJUnitRunner.class)
public class LogbookHandlerTest {

    @Rule
    public UndertowServerRule undertow = new UndertowServerRule();

    @Spy
    private final CapturingLogbook logbook = new CapturingLogbook();

    @Mock
    private Consumer<Throwable> writeFailureHandler;

    @Spy
    @InjectMocks
    private LogbookHandler underTest;

    @Before
    public void setUp() {
        doAnswer(invocation -> {
            throw Throwables.propagate(invocation.getArgumentAt(0, Throwable.class));
        }).when(writeFailureHandler).accept(any());

        underTest.setNext(ResponseCodeHandler.HANDLE_200);
        undertow.setHandler(underTest);
    }

    @Test
    public void shouldLogRequestAndResponse() throws IOException {
        undertow.sendRequest(Methods.GET, "/", new HeaderMap());

        assertThat(logbook.getCaptures(), hasSize(1));
        final CapturedCorrelation captured = logbook.getCaptures().get(0);

        final HttpRequest request = captured.getRequest();

        assertThat(request.getRemote(), is("127.0.0.1"));
        assertThat(request.getMethod(), is("GET"));
        assertThat(request.getRequestUri(), is("/"));
        assertThat(request.getHeaders(), hasProperty("empty", is(true)));
        assertThat(request.getContentType(), is(""));
        assertThat(request.getCharset(), is(StandardCharsets.UTF_8));
        assertThat(request.getBody().length, is(0));
        assertThat(request.getBodyAsString(), is(""));

        final HttpResponse response = captured.getResponse();

        assertThat(response.getStatus(), is(StatusCodes.OK));
        assertThat(response.getHeaders().asMap(),
                allOf(hasKey("Date"), hasKey("Content-Length"), hasKey("Connection")));
        assertThat(response.getContentType(), is(""));
        assertThat(response.getCharset(), is(StandardCharsets.UTF_8));
        assertThat(response.getBody().length, is(0));
        assertThat(response.getBodyAsString(), is(""));
    }

    @Test
    public void requestUriShouldIncludeQueryString() throws IOException {
        final HeaderMap headers = new HeaderMap();
        headers.add(Headers.HOST, "foo.example.com");

        undertow.sendRequest(Methods.GET, "/?bar=baz", headers);

        assertThat(logbook.getCaptures(), hasSize(1));
        final CapturedCorrelation captured = logbook.getCaptures().get(0);
        final HttpRequest request = captured.getRequest();
        assertThat(request.getRequestUri(), is("/?bar=baz"));
        assertThat(request.getHeaders().entries(), hasSize(1));
        assertThat(request.getHeaders().asMap(), hasEntry(is(Headers.HOST_STRING), contains("foo.example.com")));
    }

    @Test
    public void shouldHonorRequestCharset() throws IOException {
        final HeaderMap headers = new HeaderMap();
        final String contentType = "text/plain; charset=UTF-16";
        headers.add(Headers.CONTENT_TYPE, contentType);

        undertow.sendRequest(Methods.GET, "/", headers);

        assertThat(logbook.getCaptures(), hasSize(1));
        final CapturedCorrelation captured = logbook.getCaptures().get(0);
        assertThat(captured.getRequest(), allOf(hasProperty("contentType", is(contentType)),
                hasProperty("charset", is(StandardCharsets.UTF_16))));
    }

    @Test
    public void shouldLogNothingIfNotActive() throws IOException {
        logbook.deactivate();

        undertow.sendRequest(Methods.GET, "/", new HeaderMap());

        assertThat(logbook.getCaptures(), is(empty()));
    }

    @Test
    public void shouldHandleRequestWriteException() throws IOException {
        final IOException toBeThrown = new IOException("mocked write failure");
        doThrow(toBeThrown).when(logbook).write(any());
        // clear exception forwarding
        doAnswer(invocation -> null).when(writeFailureHandler).accept(any());

        undertow.sendRequest(Methods.GET, "/", new HeaderMap());

        verify(writeFailureHandler).accept(toBeThrown);
        assertThat(logbook.getCaptures(), is(empty()));
    }

    @Test
    public void shouldHandleResponseWriteException() throws Throwable {
        final IOException toBeThrown = new IOException("mocked write failure");
        doAnswer(invocation -> {
            invocation.callRealMethod();
            return Optional.<Correlator> of(response -> {
                throw toBeThrown;
            });
        }).when(logbook).write(any());
        // clear exception forwarding
        doAnswer(invocation -> null).when(writeFailureHandler).accept(any());

        undertow.sendRequest(Methods.GET, "/", new HeaderMap());

        verify(writeFailureHandler).accept(toBeThrown);
        assertThat(logbook.getCaptures(), contains(hasProperty("responseCaptured", is(false))));
    }

    @Test
    public void shouldNotFailOnMissingCorrelator() {
        final HttpServerExchange exchange = new HttpServerExchange(mock(ServerConnection.class));
        final ExchangeCompletionListener.NextListener nextListener = mock(ExchangeCompletionListener.NextListener.class);

        underTest.exchangeEvent(exchange, nextListener);
        verify(nextListener).proceed();
    }

    @Test
    public void shouldCallNextListenerOnResponseLogFailure() throws IOException {
        final HttpServerExchange exchange = new HttpServerExchange(mock(ServerConnection.class));
        final ExchangeCompletionListener.NextListener nextListener = mock(
                ExchangeCompletionListener.NextListener.class);

        final IOException toBeThrown = new IOException("mocked write failure");
        final Correlator failingCorrelator = mock(Correlator.class);
        doThrow(toBeThrown).when(failingCorrelator).write(any());

        underTest.storeCorrelator(exchange, failingCorrelator);

        Throwable caught = null;
        try {
            underTest.exchangeEvent(exchange, nextListener);
        } catch (final Throwable t) {
            caught = t;
        }

        assertThat(caught, hasCause(is(toBeThrown)));
        verify(nextListener).proceed();
    }
}
