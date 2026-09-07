package org.zalando.logbook.autoconfigure.webflux.condition;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;

/**
 * Matches when incoming requests should be logged by {@code LogbookWebFilter}, which is the case when Reactor Netty
 * is absent or when {@code logbook.reactive.server-mode} is set to {@code web-filter}.
 */
public class ConditionalOnWebFilterServerMode extends SpringBootCondition {

    private static final String PROPERTY = "logbook.reactive.server-mode";
    private static final String WEB_FILTER = "web-filter";
    private static final String NETTY_HTTP_SERVER = "reactor.netty.http.server.HttpServer";

    @Override
    public ConditionOutcome getMatchOutcome(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        return usesWebFilter(context)
                ? ConditionOutcome.match("Reactive server mode is web-filter")
                : ConditionOutcome.noMatch("Reactive server mode is netty");
    }

    /**
     * The single source of truth for both server mode conditions, so that exactly one of them ever matches.
     * An unrecognized value keeps the Reactor Netty handler in place.
     */
    static boolean usesWebFilter(final ConditionContext context) {
        return !ClassUtils.isPresent(NETTY_HTTP_SERVER, context.getClassLoader())
                || WEB_FILTER.equalsIgnoreCase(context.getEnvironment().getProperty(PROPERTY));
    }

}
