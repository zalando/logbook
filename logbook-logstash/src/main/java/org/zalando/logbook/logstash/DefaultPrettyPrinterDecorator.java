package org.zalando.logbook.logstash;

import com.fasterxml.jackson.core.JsonGenerator;
import net.logstash.logback.decorate.JsonGeneratorDecorator;

public final class DefaultPrettyPrinterDecorator implements JsonGeneratorDecorator {

    @Override
    public JsonGenerator decorate(final JsonGenerator generator) {
        return generator.useDefaultPrettyPrinter();
    }

}
