package org.zalando.logbook.autoconfigure.webflux.condition;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Matches when incoming requests should be logged by the Reactor Netty handler, the exact complement of
 * {@link ConditionalOnWebFilterServerMode}.
 */
public class ConditionalOnNettyServerMode extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        return ConditionalOnWebFilterServerMode.usesWebFilter(context)
                ? ConditionOutcome.noMatch("Reactive server mode is web-filter")
                : ConditionOutcome.match("Reactive server mode is netty");
    }

}
