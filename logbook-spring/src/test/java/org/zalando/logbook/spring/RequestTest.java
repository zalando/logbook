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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;

import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class RequestTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private final HttpRequest delegate = mock(HttpRequest.class);
    private final Localhost localhost = mock(Localhost.class);
    private final Request unit = new Request(delegate, new byte[0], localhost);

    @Before
    public void defaultBehaviour() {
        when(delegate.getHeaders()).thenReturn(new HttpHeaders());
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
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/plain;charset=ISO-8859-1"));
        when(delegate.getHeaders()).thenReturn(headers);

        assertThat(unit.getCharset(), is(StandardCharsets.ISO_8859_1));
    }

    @Test
    public void shouldReturnDefaultCharsetIfNoneGiven() {
        assertThat(unit.getCharset(), is(StandardCharsets.UTF_8));
    }

}