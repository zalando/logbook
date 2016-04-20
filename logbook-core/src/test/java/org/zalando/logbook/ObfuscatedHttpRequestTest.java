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

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public final class ObfuscatedHttpRequestTest {

    private final HttpRequest unit = new ObfuscatedHttpRequest(MockHttpRequest.builder()
            .requestUri("/")
            .queryParameters(ImmutableListMultimap.of(
                    "password", "1234",
                    "limit", "1"
            ))
            .headers(ImmutableListMultimap.of(
                    "Authorization", "Bearer 9b7606a6-6838-11e5-8ed4-10ddb1ee7671",
                    "Accept", "text/plain"))
            .body("My secret is s3cr3t")
            .build(),
            Obfuscator.authorization(),
            Obfuscator.obfuscate("password"::equalsIgnoreCase, "unknown"),
            (contentType, body) -> body.replace("s3cr3t", "f4k3"));

    @Test
    public void shouldNotFailOnInvalidUri() {
        final String invalidUri = "/af.cgi?_browser_out=.|.%2F.|.%2F.|.%2F.|.%2F.|.%2F.|.%2F.|.%2F.|.%2F.|.%2F.|.%2F.|.%2F.|.%2Fetc%2Fpasswd";
        final ObfuscatedHttpRequest invalidRequest = new ObfuscatedHttpRequest(
                MockHttpRequest.builder()
                        .requestUri(invalidUri)
                        .build(),
                Obfuscator.none(),
                Obfuscator.obfuscate("_browser_out"::equalsIgnoreCase, "unknown"),
                BodyObfuscator.none());

        assertThat(invalidRequest.getRequestUri(), is(invalidUri));
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
                Obfuscator.none(),
                Obfuscator.obfuscate(x -> true, "*"),
                BodyObfuscator.none());

        assertThat(request.getRequestUri(), is("http://localhost/"));
    }

    @Test
    public void shouldObfuscatePasswordParameter() {
        assertThat(unit.getQueryParameters().asMap(), hasEntry("password", singletonList("unknown")));
    }

    @Test
    public void shouldNotObfuscateLimitParameter() {
        assertThat(unit.getQueryParameters().asMap(), hasEntry("limit", singletonList("1")));
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