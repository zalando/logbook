package org.zalando.logbook.autoconfigure.logging;

import org.springframework.boot.json.JsonWriter;
import org.springframework.boot.logging.structured.StructuredLoggingJsonMembersCustomizer;

import java.util.HashMap;
import java.util.Map;

public class EcsStructuredLoggingJsonMembersCustomizer implements StructuredLoggingJsonMembersCustomizer<Object> {

    @Override
    public void customize(JsonWriter.Members<Object> members) {
        members.addMapEntries(ignored -> new HashMap<>(NativeEcsStructuredHttpLogFormatter.ECS_STRUCTURED_MEMBERS.get()));
    }

}
