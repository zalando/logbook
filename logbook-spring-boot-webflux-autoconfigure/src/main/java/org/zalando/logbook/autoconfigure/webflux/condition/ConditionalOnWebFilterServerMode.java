package org.zalando.logbook.autoconfigure.webflux.condition;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;
import org.zalando.logbook.autoconfigure.LogbookProperties.ServerMode;

/**
 * Matches when incoming requests should be logged by {@code LogbookWebFilter}, which is the case when Reactor Netty
 * is absent or when {@code logbook.reactive.server-mode} is set to {@code web-filter}.
 */
public class ConditionalOnWebFilterServerMode extends SpringBootCondition {

    private static final String PROPERTY = "logbook.reactive.server-mode";
    private static final String NETTY_HTTP_SERVER = "reactor.netty.http.server.HttpServer";

    @Override
    public ConditionOutcome getMatchOutcome(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        return usesWebFilter(context)
                ? ConditionOutcome.match("Reactive server mode is web-filter")
                : ConditionOutcome.noMatch("Reactive server mode is netty");
    }

    static boolean usesWebFilter(final ConditionContext context) {
        final ServerMode serverMode = Binder.get(context.getEnvironment())
                .bind(PROPERTY, ServerMode.class)
                .orElse(ServerMode.NETTY);

        return serverMode == ServerMode.WEB_FILTER
                || !ClassUtils.isPresent(NETTY_HTTP_SERVER, context.getClassLoader());
    }

}
