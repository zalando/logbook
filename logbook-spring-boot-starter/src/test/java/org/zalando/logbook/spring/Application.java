package org.zalando.logbook.spring;

import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.test.ImportAutoConfiguration;

@ImportAutoConfiguration({LogbookAutoConfiguration.class, JacksonAutoConfiguration.class})
public class Application {
}
