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

import org.junit.Test;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.zalando.logbook.Origin.LOCAL;

public final class ForwardingHttpResponseTest {

    private final HttpResponse unit = new ForwardingHttpResponse() {
        @Override
        protected HttpResponse delegate() {
            final HttpResponse response = mock(HttpResponse.class);

            when(response.getOrigin()).thenReturn(LOCAL);
            when(response.getStatus()).thenReturn(200);
            when(response.getHeaders()).thenReturn(emptyMap());
            when(response.getContentType()).thenReturn("");
            when(response.getCharset()).thenReturn(UTF_8);

            try {
                when(response.getBody()).thenReturn("".getBytes(UTF_8));
                when(response.getBodyAsString()).thenReturn("");
            } catch (final IOException e) {
                throw new AssertionError(e);
            }

            return response;
        }
    };

    @Test
    public void shouldDelegate() throws IOException {
        assertThat(unit.getOrigin(), is(LOCAL));
        assertThat(unit.getStatus(), is(200));
        assertThat(unit.getHeaders().values(), is(empty()));
        assertThat(unit.getContentType(), is(emptyString()));
        assertThat(unit.getCharset(), is(UTF_8));
        assertThat(unit.getBody(), is("".getBytes(UTF_8)));
        assertThat(unit.getBodyAsString(), is(emptyString()));
        assertNotNull(unit.toString());
    }

}
