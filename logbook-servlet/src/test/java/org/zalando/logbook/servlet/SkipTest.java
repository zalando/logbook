package org.zalando.logbook.servlet;

/*
 * #%L
 * logbook
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
import org.zalando.logbook.Correlation;
import org.zalando.logbook.DefaultHttpLogFormatter;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;

import java.io.IOException;

import static com.jayway.restassured.RestAssured.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class SkipTest {

    private final HttpLogFormatter formatter = spy(new ForwardingHttpLogFormatter(new DefaultHttpLogFormatter()));
    private final HttpLogWriter writer = mock(HttpLogWriter.class);

    @Rule
    public final ServerRule server = new ServerRule(new LogbookFilter(Logbook.builder()
            .formatter(formatter)
            .writer(writer)
            .build()));

    @Before
    public void setUp() throws IOException {
        when(writer.isActive(any())).thenReturn(false);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldNotLogRequest() throws Exception {
        given().when().post(server.url("/echo"));

        verify(formatter, never()).format(any(Precorrelation.class));
        verify(writer, never()).writeRequest(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldNotLogResponse() throws Exception {
        given().when().post(server.url("/echo"));

        verify(formatter, never()).format(any(Correlation.class));
        verify(writer, never()).writeRequest(any());
    }

}
