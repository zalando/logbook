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

import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public final class MockRawHttpRequestTest implements MockHttpMessageTester {

    private final RawHttpRequest unit = MockRawHttpRequest.create();

    @Test
    public void shouldDelegate() throws IOException {
        verifyRequest(unit);
    }

    @Test
    public void shouldDelegateWithBody() throws IOException {
        final HttpRequest request = unit.withBody();
        verifyRequest(request);
        assertThat(request.getBody(), is("".getBytes(UTF_8)));
        assertThat(request.getBodyAsString(), is(""));
    }

    @Test
    public void shouldUseNonDefaultPort() {
        final MockRawHttpRequest unit = MockRawHttpRequest.request().port(8080).build();

        assertThat(unit.getPort(), is(Optional.of(8080)));
    }

}