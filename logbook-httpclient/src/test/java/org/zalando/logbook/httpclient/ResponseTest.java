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

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public final class ResponseTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private final HttpResponse delegate = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "OK");
    private final Response unit = new Response(delegate);

    @Test
    public void shouldReturnContentTypesCharsetIfGiven() {
        delegate.addHeader("Content-Type", "text/plain;charset=ISO-8859-1");
                
        assertThat(unit.getCharset(), is(StandardCharsets.ISO_8859_1));
    }

    @Test
    public void shouldReturnDefaultCharsetIfNoneGiven() {
        assertThat(unit.getCharset(), is(StandardCharsets.UTF_8));
    }
    
    @Test
    public void shouldNotReadEmptyBodyIfNotPresent() throws IOException {
        assertThat(new String(unit.withBody().getBody(), UTF_8), is(emptyString()));
        assertThat(delegate.getEntity(), is(nullValue()));
    }

}