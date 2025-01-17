package org.zalando.logbook.json;

import com.fasterxml.jackson.core.JsonGenerator;

final class NumberAsStringJsonGeneratorWrapper extends JsonGeneratorWrapper {
    @SuppressWarnings("deprecation")
    public NumberAsStringJsonGeneratorWrapper(JsonGenerator delegate) {
        super(delegate);
        delegate.enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
    }
}
