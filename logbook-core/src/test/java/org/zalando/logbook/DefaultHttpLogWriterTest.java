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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.logbook.DefaultLogbook.SimplePrecorrelation;

import java.io.IOException;
import java.time.Duration;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class DefaultHttpLogWriterTest {

    @Test
    public void shouldDefaultToLogbookLogger() {
        final DefaultHttpLogWriter unit = new DefaultHttpLogWriter();

        assertThat(unit.getLogger(), is(equalTo(LoggerFactory.getLogger(Logbook.class))));
    }

    @Test
    public void shouldDefaultToTraceLevelForActivation() throws IOException {
        final Logger logger = mock(Logger.class);
        final HttpLogWriter unit = new DefaultHttpLogWriter(logger);

        unit.isActive(mock(RawHttpRequest.class));

        verify(logger).isTraceEnabled();
    }

    @Test
    public void shouldDefaultToTraceLevelForLoggingRequests() throws IOException {
        final Logger logger = mock(Logger.class);
        final HttpLogWriter unit = new DefaultHttpLogWriter(logger);

        unit.writeRequest(new SimplePrecorrelation<>("1", "foo"));

        verify(logger).trace("foo");
    }

    @Test
    public void shouldDefaultToTraceLevelForLoggingResponses() throws IOException {
        final Logger logger = mock(Logger.class);
        final HttpLogWriter unit = new DefaultHttpLogWriter(logger);

        unit.writeResponse(new DefaultLogbook.SimpleCorrelation<>("1", "foo", "bar", Duration.ZERO));

        verify(logger).trace("bar");
    }

}
