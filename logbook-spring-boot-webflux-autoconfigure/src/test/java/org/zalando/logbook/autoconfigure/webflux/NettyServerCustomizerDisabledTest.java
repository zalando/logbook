package org.zalando.logbook.autoconfigure.webflux;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.zalando.logbook.autoconfigure.webflux.LogbookWebFluxAutoConfiguration.WebFluxNettyServerConfiguration.CUSTOMIZER_NAME;

@LogbookWebfluxTest(properties = "logbook.filter.enabled=false")
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class NettyServerCustomizerDisabledTest {

    @Autowired(required = false)
    @Qualifier(CUSTOMIZER_NAME)
    private NettyServerCustomizer nettyServerCustomizer;

    @Test
    void shouldNotInitializeFilter() {
        assertThat(nettyServerCustomizer, is(nullValue()));
    }
}