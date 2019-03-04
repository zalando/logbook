package org.zalando.logbook.spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.logbook.ChunkingSink;
import org.zalando.logbook.Sink;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

@LogbookTest(properties = "logbook.write.chunk-size = 100")
class WriteChunkingTest {

    @Autowired
    private Sink sink;

    @Test
    void shouldUseChunkingSink() {
        assertThat(sink, is(instanceOf(ChunkingSink.class)));
    }

}
