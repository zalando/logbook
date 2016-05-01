package org.zalando.logbook;

/*
 * #%L
 * Logbook: API
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.zalando.logbook.RequestURI.Component.AUTHORITY;
import static org.zalando.logbook.RequestURI.Component.PATH;
import static org.zalando.logbook.RequestURI.Component.QUERY;
import static org.zalando.logbook.RequestURI.Component.SCHEME;
import static org.zalando.logbook.RequestURI.reconstruct;

@RunWith(MockitoJUnitRunner.class)
public final class RequestURITest {

    @Mock
    private RawHttpRequest request;

    @Before
    public void setUp() {
        when(request.getScheme()).thenReturn("http");
        when(request.getHost()).thenReturn("localhost");
        when(request.getPort()).thenReturn(80);
        when(request.getPath()).thenReturn("/admin");
        when(request.getQuery()).thenReturn("limit=1");

    }

    @Test
    public void shouldReconstructFully() {
        assertThat(reconstruct(request), is("http://localhost/admin?limit=1"));
    }

    @Test
    public void shouldNotIncludeStandardHttpsPort() {
        when(request.getScheme()).thenReturn("https");
        when(request.getPort()).thenReturn(443);
        assertThat(reconstruct(request), is("https://localhost/admin?limit=1"));
    }

    @Test
    public void shouldIncludeNonStandardHttpPort() {
        when(request.getPort()).thenReturn(8080);
        assertThat(reconstruct(request), is("http://localhost:8080/admin?limit=1"));
    }

    @Test
    public void shouldIncludeNonStandardHttpsPort() {
        when(request.getScheme()).thenReturn("https");
        when(request.getPort()).thenReturn(1443);
        assertThat(reconstruct(request), is("https://localhost:1443/admin?limit=1"));
    }

    @Test
    public void shouldReconstructWithoutSchema() {
        assertThat(reconstruct(request, AUTHORITY, PATH, QUERY), is("//localhost/admin?limit=1"));

    }

    @Test
    public void shouldReconstructWithoutAuthority() {
        assertThat(reconstruct(request, SCHEME, PATH, QUERY), is("http:///admin?limit=1"));
    }

    @Test
    public void shouldReconstructWithoutSchemeAndAuthority() {
        assertThat(reconstruct(request, PATH, QUERY), is("/admin?limit=1"));
    }

    @Test
    public void shouldReconstructWithoutPath() {
        assertThat(reconstruct(request, SCHEME, AUTHORITY, QUERY), is("http://localhost/?limit=1"));
    }

    @Test
    public void shouldReconstructWithoutQuery() {
        assertThat(reconstruct(request, SCHEME, AUTHORITY, PATH), is("http://localhost/admin"));
    }

    @Test
    public void shouldReconstructWithoutEmptyQuery() {
        when(request.getQuery()).thenReturn("");

        assertThat(reconstruct(request), is("http://localhost/admin"));
    }

    @Test
    public void shouldReconstructWithoutSchemeAuthorityAndPath() {
        assertThat(reconstruct(request, QUERY), is("/?limit=1"));
    }

}