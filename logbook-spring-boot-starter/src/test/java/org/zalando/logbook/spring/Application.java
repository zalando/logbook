package org.zalando.logbook.spring;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;

@ImportAutoConfiguration({
        LogbookAutoConfiguration.class,
        JacksonAutoConfiguration.class
})
public class Application {
}
