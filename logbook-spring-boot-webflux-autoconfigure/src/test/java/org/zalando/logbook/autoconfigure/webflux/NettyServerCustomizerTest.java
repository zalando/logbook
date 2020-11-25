package org.zalando.logbook.autoconfigure.webflux;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.zalando.logbook.autoconfigure.webflux.LogbookWebFluxAutoConfiguration.WebFluxNettyServerConfiguration.CUSTOMIZER_NAME;

@LogbookWebfluxTest
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class NettyServerCustomizerTest {

    @Autowired
    @Qualifier(CUSTOMIZER_NAME)
    private NettyServerCustomizer nettyServerCustomizer;

    @Test
    void shouldInitializeFilter() {
        assertThat(nettyServerCustomizer, is(notNullValue()));
    }
}