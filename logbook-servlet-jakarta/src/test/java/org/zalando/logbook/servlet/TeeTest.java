package org.zalando.logbook.servlet;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zalando.logbook.DefaultHttpLogFormatter;
import org.zalando.logbook.DefaultSink;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.Configuration.Defaults;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;

import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.EnumSet;
import java.util.Set;

/**
 * Verifies that {@link LogbookFilter} handles the copying of streams in {@link RemoteRequest} and {@link LocalResponse}
 * correctly.
 */
final class TeeTest {

    private final HttpLogFormatter formatter = spy(new ForwardingHttpLogFormatter(new DefaultHttpLogFormatter()));
    private final HttpLogWriter writer = mock(HttpLogWriter.class);

    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new ExampleController())
            .addFilter(new LogbookFilter(Logbook.builder()
                    .sink(new DefaultSink(formatter, writer))
                    .build()))
            .build();

    @BeforeAll
    static void beforeAll() {
        Configuration.setDefaults(new Defaults() {

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }

            @Override
            public MappingProvider mappingProvider() {
                return new JacksonMappingProvider();
            }

            @Override
            public JsonProvider jsonProvider() {
                return new JacksonJsonProvider();
            }
        });
    }

    @BeforeEach
    void setUp() {
        reset(formatter, writer);

        when(writer.isActive()).thenReturn(true);
    }

    @Test
    void shouldWriteResponse() throws Exception {
        mvc.perform(get("/api/sync"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", startsWith("application/json")))
                .andExpect(jsonPath("$.value").value("Hello, world!"));
    }

    @Test
    void shouldSupportReadSingleByte() throws Exception {
        mvc.perform(get("/api/read-byte")
                .contentType(MediaType.TEXT_PLAIN)
                .content(new byte[]{17}))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[]{17}));
    }

    @Test
    void shouldSupportReadByte() throws Exception {
        mvc.perform(get("/api/read-byte")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Hello, world!"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, world!"));
    }

    @Test
    void shouldSupportReadBytes() throws Exception {
        mvc.perform(get("/api/read-bytes")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Hello, world!"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, world!"));
    }

    @Test
    void shouldSupportStream() throws Exception {
        mvc.perform(get("/api/stream")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Hello, world!"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, world!"));
    }

    @Test
    void shouldSupportReader() throws Exception {
        mvc.perform(get("/api/reader")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Hello, world!"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, world!"));
    }

}
