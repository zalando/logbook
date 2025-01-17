package org.zalando.logbook.json;

import com.fasterxml.jackson.core.JsonGenerator;

final class DefaultJsonGeneratorWrapper extends JsonGeneratorWrapper {
    public DefaultJsonGeneratorWrapper(JsonGenerator delegate) {
        super(delegate);
    }
}
