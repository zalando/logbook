package org.zalando.logbook.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.StringWriter;

// TODO make it an interface and offer two implementations: discard newlines vs. parse json
@AllArgsConstructor
final class JsonCompactor {

    private final ObjectMapper mapper;

    // this wouldn't catch spaces in json, but that's ok for our use case here
    boolean isCompacted(final String json) {
        return json.indexOf('\n') == -1;
    }

    // TODO json.replace("\n", "") would cover our biggest use case
    String compact(final String json) throws IOException {
        final StringWriter output = new StringWriter(json.length());
        final JsonFactory factory = mapper.getFactory();
        final JsonParser parser = factory.createParser(json);

        final JsonGenerator generator = factory.createGenerator(output);

        // https://github.com/jacoco/jacoco/wiki/FilteringOptions
        //noinspection TryFinallyCanBeTryWithResources - jacoco can't handle try-with correctly
        try {
            while (parser.nextToken() != null) {
                generator.copyCurrentEvent(parser);
            }
        } finally {
            generator.close();
        }

        return output.toString();
    }

}
