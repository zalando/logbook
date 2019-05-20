package org.zalando.logbook.json;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.zalando.logbook.BodyFilter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Thread-safe filter for JSON fields. Filters on property names, 
 *
 */

public class JacksonJsonFieldBodyFilter implements BodyFilter {

    private final String replacement;
    private final Set<String> fields;
    private final ObjectMapper objectMapper;
    
    public JacksonJsonFieldBodyFilter(Collection<String> fieldNames, String replacement, ObjectMapper objectMapper) {
        this.fields = new HashSet<>(fieldNames); // thread safe for reading
        this.replacement = replacement;
        this.objectMapper = objectMapper;
    }

    public JacksonJsonFieldBodyFilter(Collection<String> fieldNames, String replacement) {
        this(fieldNames, replacement, new ObjectMapper());
    }

    @Override
    public String filter(String contentType, String body) {
        return JsonMediaType.JSON.test(contentType) ? filter(body) : body;
    }

    public String filter(final String body) {
        try {
            JsonFactory factory = objectMapper.getFactory();
            final JsonParser parser = factory.createParser(body);
            
            StringWriter writer = new StringWriter(body.length() * 2); // rough estimate of final size
            
            JsonGenerator generator = factory.createGenerator(writer);            
            try {
                do {
                    JsonToken nextToken = parser.nextToken();
                    if(nextToken == null) {
                        break;
                    }

                    generator.copyCurrentEvent(parser);
                    if(nextToken == JsonToken.FIELD_NAME && fields.contains(parser.getCurrentName())) {
                        nextToken = parser.nextToken();
                        generator.writeString(replacement);
                        if(!nextToken.isScalarValue()) {
                            parser.skipChildren(); // skip children
                        }
                    }
                    
                } while(true);
            } finally {
                parser.close();
                
                generator.close();
            }
            
            return writer.toString();
        } catch(Exception e) {
            // ignore
        }
        return body;
}


}
