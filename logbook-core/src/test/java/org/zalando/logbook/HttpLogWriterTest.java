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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class HttpLogWriterTest {

    @Test
    public void shouldBeActiveByDefault() throws IOException {
        final HttpLogWriter unit = new MockHttpLogWriter();

        assertThat(unit.isActive(mock(RawHttpRequest.class)), is(true));
    }

    private static class MockHttpLogWriter implements HttpLogWriter {

        @Override
        public void writeRequest(final Precorrelation<String> precorrelation) throws IOException {

        }

        @Override
        public void writeResponse(final Correlation<String, String> correlation) throws IOException {

        }

    }

}
