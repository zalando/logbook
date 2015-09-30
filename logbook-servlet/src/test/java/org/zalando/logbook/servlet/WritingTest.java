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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Verifies that {@link LogbookFilter} delegates to {@link HttpLogWriter} correctly.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
@WebAppConfiguration
public final class WritingTest {

    private final String url = "/api/sync";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private HttpLogFormatter formatter;

    @Autowired
    private HttpLogWriter writer;

    @Before
    public void setUp() throws IOException {
        reset(formatter, writer);

        when(writer.isActive(any())).thenReturn(true);
    }

    @Test
    public void shouldLogRequest() throws Exception {
        mvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON)
                .header("Host", "localhost")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Hello, world!"));

        verify(writer).writeRequest("GET /api/sync HTTP/1.1\n" +
                "Accept: application/json\n" +
                "Host: localhost\n" +
                "Content-Type: text/plain\n" +
                "\n" +
                "Hello, world!");
    }

    @Test
    public void shouldLogResponse() throws Exception {
        mvc.perform(get(url));

        verify(writer).writeResponse("HTTP/1.1 200\n" +
                "Content-Type: application/json\n" +
                "\n" +
                "{\"value\":\"Hello, world!\"}");
    }

}
