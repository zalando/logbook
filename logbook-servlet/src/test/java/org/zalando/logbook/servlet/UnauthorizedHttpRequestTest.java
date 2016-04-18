package org.zalando.logbook.servlet;

/*
 * #%L
 * Logbook: Servlet
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
import org.zalando.logbook.HttpRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static java.util.Collections.emptyEnumeration;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class UnauthorizedHttpRequestTest {

    private final HttpRequest unit;

    public UnauthorizedHttpRequestTest() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeaderNames()).thenReturn(emptyEnumeration());
        this.unit = new UnauthorizedHttpRequest(new TeeRequest(request));
    }

    @Test
    public void shouldRemoveBody() throws IOException {
        assertThat(unit.getBody().length, is(0));
    }

    @Test
    public void shouldRemoveBodyAsString() throws IOException {
        assertThat(unit.getBodyAsString(), is(emptyString()));
    }
    
}