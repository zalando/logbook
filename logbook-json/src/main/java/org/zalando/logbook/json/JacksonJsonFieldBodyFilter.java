package org.zalando.logbook.json;

import java.io.StringWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.zalando.logbook.BodyFilter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * Thread-safe filter for JSON fields. Filters on property names.
 * <br><br> 
 * Output is always compacted, even in case of invalid JSON, 
 * so this filter should not be used in conjunction with {@linkplain JsonCompactor}.
 *
 */

@Slf4j
public class JacksonJsonFieldBodyFilter implements BodyFilter {

    private final static StringReplaceJsonCompactor fallbackCompactor = new StringReplaceJsonCompactor();

    private final String replacement;
    private final Set<String> fields;
    private final JsonFactory factory;

    public JacksonJsonFieldBodyFilter(Collection<String> fieldNames, String replacement, ObjectMapper objectMapper) {
        this.fields = new HashSet<>(fieldNames); // thread safe for reading
        this.replacement = replacement;
        this.factory = objectMapper.getFactory();
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
            final JsonParser parser = factory.createParser(body);
            
            StringWriter writer = new StringWriter(body.length() * 2); // rough estimate of final size
            
            JsonGenerator generator = factory.createGenerator(writer);            
            try {
                while(true) {
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
                }                    
            } finally {
                parser.close();
                
                generator.close();
            }
            
            return writer.toString();
        } catch(Exception e) {
            log.trace("Unable to filter body for fields {}, compacting result. `{}`", fields, e.getMessage()); 
            return fallbackCompactor.compact(body);
        }
    }

}
