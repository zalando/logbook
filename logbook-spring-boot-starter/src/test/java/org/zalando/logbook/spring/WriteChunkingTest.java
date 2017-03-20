package org.zalando.logbook.spring;

import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.zalando.logbook.ChunkingHttpLogWriter;
import org.zalando.logbook.HttpLogWriter;

import java.io.IOException;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.junit.Assert.assertThat;

@TestPropertySource(properties = "logbook.write.chunk-size = 100")
public final class WriteChunkingTest extends AbstractTest {

    @Autowired
    private HttpLogWriter writer;

    @Test
    public void shouldUseChunkingWriter() throws IOException {
        assertThat(writer, is(instanceOf(ChunkingHttpLogWriter.class)));
    }

}
