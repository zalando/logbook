package org.zalando.logbook.httpclient;

/*
 * #%L
 * Logbook: HTTP Client
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


import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import static com.google.common.io.ByteStreams.toByteArray;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class RequestTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private final HttpEntityEnclosingRequest delegate = new BasicHttpEntityEnclosingRequest("GET", "http://localhost/");
    private final Localhost localhost = mock(Localhost.class);
    private final Request unit = new Request(delegate, localhost);

    @Test
    public void shouldResolveLocalhost() {
        final Request unit = new Request(delegate, Localhost.resolve());
        
        assertThat(unit.getRemote(), matchesPattern("(\\d{1,3}\\.){3}\\d{1,3}"));
    }
    
    @Test
    public void shouldHandleUnknownHostException() throws UnknownHostException {
        when(localhost.getAddress()).thenThrow(new UnknownHostException());

        exception.expect(IllegalStateException.class);
        exception.expectCause(instanceOf(UnknownHostException.class));

        unit.getRemote();
    }

    @Test
    public void shouldReturnContentTypesCharsetIfGiven() {
        delegate.addHeader("Content-Type", "text/plain;charset=ISO-8859-1");
        assertThat(unit.getCharset(), is(StandardCharsets.ISO_8859_1));
    }

    @Test
    public void shouldReturnDefaultCharsetIfNoneGiven() {
        assertThat(unit.getCharset(), is(UTF_8));
    }
    
    @Test
    public void shouldReadBodyIfPresent() throws IOException {
        delegate.setEntity(new StringEntity("Hello, world!", UTF_8));
        
        assertThat(new String(unit.withBody().getBody(), UTF_8), is("Hello, world!"));
        assertThat(new String(toByteArray(delegate.getEntity().getContent()), UTF_8), is("Hello, world!"));
    }

}