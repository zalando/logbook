package org.zalando.logbook.autoconfigure.webflux;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.reactive.function.client.ReactorNettyHttpClientMapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.zalando.logbook.autoconfigure.webflux.LogbookWebFluxAutoConfiguration.WebFluxNettyClientConfiguration.CUSTOMIZER_NAME;

@LogbookWebfluxTest
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class NettyClientCustomizerTest {

    @Autowired
    @Qualifier(CUSTOMIZER_NAME)
    private ReactorNettyHttpClientMapper nettyClientCustomizer;

    @Test
    void shouldInitializeFilter() {
        assertThat(nettyClientCustomizer, is(notNullValue()));
    }
}