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

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintStream;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.contrib.java.lang.system.SystemOutRule;

public final class StreamHttpLogWriterTest {

    @Rule
    public final SystemOutRule stdout = new SystemOutRule().enableLog();

    @Test
    public void shouldBeActiveByDefault() throws IOException {
        final PrintStream stream = mock(PrintStream.class);
        final HttpLogWriter unit = new StreamHttpLogWriter(stream);

        assertThat(unit.isActive(mock(RawHttpRequest.class)), is(true));
    }

    @Test
    public void shouldLogRequestToStream() throws IOException {
        final PrintStream stream = mock(PrintStream.class);
        final HttpLogWriter unit = new StreamHttpLogWriter(stream);

        unit.writeRequest("foo");

        verify(stream).println("foo");
    }

    @Test
    public void shouldLogResponseToStream() throws IOException {
        final PrintStream stream = mock(PrintStream.class);
        final HttpLogWriter unit = new StreamHttpLogWriter(stream);

        unit.writeResponse("foo");

        verify(stream).println("foo");
    }

    @Test
    public void shouldRequestToStdoutByDefault() throws IOException {
        final HttpLogWriter unit = new StreamHttpLogWriter();

        unit.writeRequest("foo");

        assertThat(stdout.getLog(), is("foo\n"));
    }

    @Test
    public void shouldResponseToStdoutByDefault() throws IOException {
        final HttpLogWriter unit = new StreamHttpLogWriter();

        unit.writeResponse("foo");

        assertThat(stdout.getLog(), is("foo\n"));
    }

}