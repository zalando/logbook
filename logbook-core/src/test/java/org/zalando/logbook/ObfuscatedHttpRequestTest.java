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

import com.google.common.collect.ImmutableListMultimap;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.zalando.logbook.MockHttpRequest.request;

public final class ObfuscatedHttpRequestTest {

    private final HttpRequest unit = new ObfuscatedHttpRequest(request()
            .query("password=1234&limit=1")
            .headers(ImmutableListMultimap.of(
                    "Authorization", "Bearer 9b7606a6-6838-11e5-8ed4-10ddb1ee7671",
                    "Accept", "text/plain"))
            .body("My secret is s3cr3t")
            .build(),
            Obfuscators.obfuscate("password", "unknown"),
            Obfuscators.authorization(),
            (contentType, body) -> body.replace("s3cr3t", "f4k3"));

    @Test
    public void shouldNotFailOnInvalidUri() {
        final String query = "file=.|.%2F.|.%2Fetc%2Fpasswd";

        final ObfuscatedHttpRequest invalidRequest = new ObfuscatedHttpRequest(
                MockHttpRequest.request()
                        .path("/login")
                        .query(query)
                        .build(),
                Obfuscators.obfuscate("file", "unknown"),
                HeaderObfuscator.none(),
                BodyObfuscator.none());

        assertThat(invalidRequest.getRequestUri(), endsWith("/login?file=unknown"));
        assertThat(invalidRequest.getPath(), is("/login"));
        assertThat(invalidRequest.getQuery(), is("file=unknown"));
    }

    @Test
    public void shouldObfuscateAuthorizationHeader() {
        assertThat(unit.getHeaders().asMap(), hasEntry(equalTo("Authorization"), contains("XXX")));
    }

    @Test
    public void shouldNotObfuscateAcceptHeader() {
        assertThat(unit.getHeaders().asMap(), hasEntry(equalTo("Accept"), contains("text/plain")));
    }

    @Test
    public void shouldNotObfuscateEmptyQueryString() {
        final ObfuscatedHttpRequest request = new ObfuscatedHttpRequest(MockHttpRequest.create(),
                $ -> "*",
                HeaderObfuscator.none(),
                BodyObfuscator.none());

        assertThat(request.getRequestUri(), is("http://localhost/"));
        assertThat(request.getQuery(), is(emptyString()));
    }

    @Test
    public void shouldObfuscatePasswordParameter() {
        assertThat(unit.getRequestUri(), is("http://localhost/?password=unknown&limit=1"));
        assertThat(unit.getQuery(), is("password=unknown&limit=1"));
    }

    @Test
    public void shouldObfuscateBody() throws IOException {
        assertThat(unit.getBodyAsString(), is("My secret is f4k3"));
    }

    @Test
    public void shouldObfuscateBodyContent() throws IOException {
        assertThat(new String(unit.getBody(), unit.getCharset()), is("My secret is f4k3"));
    }

}