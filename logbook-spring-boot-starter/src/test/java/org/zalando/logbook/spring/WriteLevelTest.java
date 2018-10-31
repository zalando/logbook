package org.zalando.logbook.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.MockHttpRequest;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@LogbookTest(properties = "logbook.write.level = WARN")
class WriteLevelTest {

    @Autowired
    private Logbook logbook;

    @MockBean
    private Logger logger;

    @BeforeEach
    void setUp() {
        doReturn(true).when(logger).isWarnEnabled();
    }

    @Test
    public void shouldUseConfiguredLevel() throws IOException {
        logbook.process(MockHttpRequest.create()).write();

        verify(logger).warn(anyString());
    }

}
