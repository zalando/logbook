package org.zalando.logbook.lle;


import com.fasterxml.jackson.core.JsonGenerator;

import net.logstash.logback.decorate.JsonGeneratorDecorator;

public final class PrettyPrintingDecorator implements JsonGeneratorDecorator {

    @Override
    public JsonGenerator decorate(JsonGenerator generator) {
        return generator.useDefaultPrettyPrinter();
    }

}
