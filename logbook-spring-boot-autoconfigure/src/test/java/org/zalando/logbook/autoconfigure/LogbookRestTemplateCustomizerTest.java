package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

import static org.assertj.core.api.Assertions.assertThat;

public class LogbookRestTemplateCustomizerTest {

    @LogbookTest(properties = "logbook.write.interceptors-enabled=true")
    @Nested
    class Enabled {
        @Autowired
        private RestTemplateBuilder builder;

        private RestTemplate restTemplate;

        @BeforeEach
        void setup(){
            restTemplate = builder
                    .build();
        }

        @Test
        void hasInterceptor(){
            assertThat(restTemplate.getInterceptors())
                    .anyMatch(in -> in instanceof LogbookClientHttpRequestInterceptor);
        }
    }

    @LogbookTest(properties = "logbook.write.interceptors-enabled=false")
    @Nested
    class Disabled {
        @Autowired
        private RestTemplateBuilder builder;

        private RestTemplate restTemplate;

        @BeforeEach
        void setup(){
            restTemplate = builder
                    .build();
        }

        @Test
        void noInterceptor(){
            assertThat(restTemplate.getInterceptors())
                    .noneMatch(in -> in instanceof LogbookClientHttpRequestInterceptor);
        }
    }
}
