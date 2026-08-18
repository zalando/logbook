package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

import static org.assertj.core.api.Assertions.assertThat;

@LogbookTest
class RestClientAutoConfigurationTest {

    @Autowired
    @Qualifier("logbookRestClientCustomizer")
    private RestClientCustomizer customizer;

    @Autowired
    private LogbookClientHttpRequestInterceptor interceptor;

    @Test
    void shouldAutoConfigureRestClientCustomizer() {
        assertThat(customizer).isNotNull();
        assertThat(interceptor).isNotNull();
    }

}
