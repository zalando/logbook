package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.logbook.Sink;
import org.zalando.logbook.core.ChunkingSink;

import static org.assertj.core.api.Assertions.assertThat;

@LogbookTest(properties = "logbook.write.chunk-size = 100")
class WriteChunkingTest {

    @Autowired
    private Sink sink;

    @Test
    void shouldUseChunkingSink() {
        assertThat(sink).isInstanceOf(ChunkingSink.class);
    }

}
