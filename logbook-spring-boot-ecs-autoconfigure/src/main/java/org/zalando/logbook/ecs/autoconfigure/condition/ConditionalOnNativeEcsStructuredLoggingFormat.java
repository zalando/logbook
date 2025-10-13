package org.zalando.logbook.ecs.autoconfigure.condition;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

public class ConditionalOnNativeEcsStructuredLoggingFormat extends AnyNestedCondition {

    public ConditionalOnNativeEcsStructuredLoggingFormat() {
        super(ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnProperty(name = "logging.structured.format.console", havingValue = "ecs")
    static class ConsoleFormatCondition {

    }

    @ConditionalOnProperty(name = "logging.structured.format.file", havingValue = "ecs")
    static class FileFormatCondition {

    }

}
