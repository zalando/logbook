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

import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that {@link LogbookFilter} handles the copying of streams in {@link TeeRequest} and {@link TeeResponse}
 * correctly.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
@WebAppConfiguration
public final class TeeTest {

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
    public void shouldWriteResponse() throws Exception {
        mvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.value", is("Hello, world!")));
    }

    @Test
    public void shouldSupportReadSingleByte() throws Exception {
        mvc.perform(get("/api/read-byte")
                .contentType(MediaType.TEXT_PLAIN)
                .content(new byte[]{17}))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[]{17}));
    }

    @Test
    public void shouldSupportReadByte() throws Exception {
        mvc.perform(get("/api/read-byte")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Hello, world!"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, world!"));
    }

    @Test
    public void shouldSupportReadBytes() throws Exception {
        mvc.perform(get("/api/read-bytes")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Hello, world!"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, world!"));
    }

    @Test
    public void shouldSupportStream() throws Exception {
        mvc.perform(get("/api/stream")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Hello, world!"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, world!"));
    }

    @Test
    public void shouldSupportReader() throws Exception {
        mvc.perform(get("/api/reader")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Hello, world!"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, world!"));
    }

}
