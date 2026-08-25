package org.zalando.logbook.ecs.autoconfigure;

import org.apiguardian.api.API;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.zalando.logbook.Sink;
import org.zalando.logbook.StructuredHttpLogFormatter;
import org.zalando.logbook.autoconfigure.LogbookAutoConfiguration;
import org.zalando.logbook.autoconfigure.LogbookProperties;
import org.zalando.logbook.ecs.EcsSink;
import org.zalando.logbook.ecs.EcsStructuredHttpLogFormatter;
import org.zalando.logbook.ecs.StatusCodeBasedEcsSink;
import org.zalando.logbook.ecs.autoconfigure.condition.ConditionalOnNativeEcsStructuredLoggingFormat;

import static org.apiguardian.api.API.Status.INTERNAL;

@AutoConfiguration(before = LogbookAutoConfiguration.class)
public class LogbookEcsAutoConfiguration {

    @API(status = INTERNAL)
    @Bean
    @Conditional(ConditionalOnNativeEcsStructuredLoggingFormat.class)
    @ConditionalOnMissingBean(StructuredHttpLogFormatter.class)
    StructuredHttpLogFormatter ecsStructuredHttpLogFormatter(ObjectProvider<LogbookProperties> logbookPropertiesObjectProvider) {
        return new EcsStructuredHttpLogFormatter(logbookPropertiesObjectProvider);
    }

    @API(status = INTERNAL)
    @Bean
    @Conditional(ConditionalOnNativeEcsStructuredLoggingFormat.class)
    @ConditionalOnMissingBean(Sink.class)
    @ConditionalOnBooleanProperty(value = "logbook.write.status-code-based", havingValue = false, matchIfMissing = true)
    Sink ecsSink(StructuredHttpLogFormatter ecsStructuredHttpLogFormatter) {
        return new EcsSink(ecsStructuredHttpLogFormatter);
    }

    @API(status = INTERNAL)
    @Bean
    @Conditional(ConditionalOnNativeEcsStructuredLoggingFormat.class)
    @ConditionalOnMissingBean(Sink.class)
    @ConditionalOnBooleanProperty("logbook.write.status-code-based")
    Sink statusCodeBasedEcsSink(StructuredHttpLogFormatter ecsStructuredHttpLogFormatter) {
        return new StatusCodeBasedEcsSink(ecsStructuredHttpLogFormatter);
    }
}
