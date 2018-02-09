package org.zalando.logbook.feignlogger;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.Logbook;
import org.mockito.stubbing.*;
import org.mockito.verification.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FeignLoggerTest {

    private Logbook logbook = mock(Logbook.class);
    
    @Test
    public void canConstructDefault() {
        new FeignLogger(logbook);
    }
    
    @Test
    public void canConstructFromName() {
        new FeignLogger("my.name", logbook);
    }
    
    @Test
    public void canConstructFromClass() {
        new FeignLogger(FeignLoggerTest.class, logbook);
    }

    @Test
    public void canConstructFromLogger() {
        new FeignLogger(mock(Logger.class), logbook);
    }
}
