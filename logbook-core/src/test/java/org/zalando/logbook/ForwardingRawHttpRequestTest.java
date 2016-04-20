package org.zalando.logbook;

/*
 * #%L
 * Logbook: Core
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

import com.google.common.collect.ImmutableMultimap;
import org.junit.Test;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.zalando.logbook.Origin.REMOTE;

public final class ForwardingRawHttpRequestTest {

    private final RawHttpRequest unit = new ForwardingRawHttpRequest() {

        @Override
        protected RawHttpRequest delegate() {
            return MockRawHttpRequest.create();
        }

    };

    @Test
    public void shouldDelegate() throws IOException {
        assertThat(unit.getOrigin(), is(REMOTE));
        assertThat(unit.getRemote(), is("127.0.0.1"));
        assertThat(unit.getMethod(), is("GET"));
        assertThat(unit.getRequestUri(), is("http://localhost/"));
        assertThat(unit.getQueryParameters().values(), is(empty()));
        assertThat(unit.getProtocolVersion(), is("HTTP/1.1"));
        assertThat(unit.getHeaders(), is(ImmutableMultimap.of()));
        assertThat(unit.getContentType(), is(""));
        assertThat(unit.getCharset(), is(UTF_8));
    }

    @Test
    public void shouldDelegateWithBody() throws IOException {
        final HttpRequest request = unit.withBody();

        assertThat(request.getOrigin(), is(REMOTE));
        assertThat(request.getRemote(), is("127.0.0.1"));
        assertThat(request.getMethod(), is("GET"));
        assertThat(request.getRequestUri(), is("http://localhost/"));
        assertThat(request.getQueryParameters().values(), is(empty()));
        assertThat(request.getProtocolVersion(), is("HTTP/1.1"));
        assertThat(request.getHeaders().values(), is(empty()));
        assertThat(request.getContentType(), is(""));
        assertThat(request.getCharset(), is(UTF_8));
        assertThat(request.getBody(), is("".getBytes(UTF_8)));
        assertThat(request.getBodyAsString(), is(emptyString()));
    }

}