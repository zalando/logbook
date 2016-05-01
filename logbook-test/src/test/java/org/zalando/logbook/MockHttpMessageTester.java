package org.zalando.logbook;

/*
 * #%L
 * Logbook: Test
 * %%
 * Copyright (C) 2015 - 2016 Zalando SE
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

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.zalando.logbook.Origin.LOCAL;
import static org.zalando.logbook.Origin.REMOTE;

public interface MockHttpMessageTester {

    default void verifyRequest(final BaseHttpRequest unit) throws IOException {
        assertThat(unit.getProtocolVersion(), is("HTTP/1.1"));
        assertThat(unit.getOrigin(), is(REMOTE));
        assertThat(unit.getRemote(), is("127.0.0.1"));
        assertThat(unit.getMethod(), is("GET"));
        assertThat(unit.getRequestUri(), is("http://localhost/"));
        assertThat(unit.getScheme(), is("http"));
        assertThat(unit.getHost(), is("localhost"));
        assertThat(unit.getPort(), is(80));
        assertThat(unit.getPath(), is("/"));
        assertThat(unit.getQuery(), is(emptyString()));
        assertThat(unit.getProtocolVersion(), is("HTTP/1.1"));
        assertThat(unit.getHeaders().values(), is(empty()));
        assertThat(unit.getContentType(), is(""));
        assertThat(unit.getCharset(), is(UTF_8));
    }

    default void verifyResponse(final BaseHttpResponse unit) throws IOException {
        assertThat(unit.getProtocolVersion(), is("HTTP/1.1"));
        assertThat(unit.getOrigin(), is(LOCAL));
        assertThat(unit.getStatus(), is(200));
        assertThat(unit.getHeaders().values(), is(empty()));
        assertThat(unit.getContentType(), is(emptyString()));
        assertThat(unit.getCharset(), is(UTF_8));
    }

}
