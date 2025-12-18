package org.zalando.logbook.json;

import lombok.extern.slf4j.Slf4j;
import org.zalando.logbook.BodyFilter;
import org.zalando.logbook.ContentType;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.ObjectWriteContext;
import tools.jackson.core.json.JsonFactory;

import jakarta.annotation.Nullable;
import java.io.CharArrayWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Thread-safe filter for JSON fields. Filters on property names.
 * <br><br>
 * Output is always compacted, even in case of invalid JSON,
 * so this filter should not be used in conjunction with {@linkplain JsonCompactor}.
 */

@Slf4j
public class JacksonJsonFieldBodyFilter implements BodyFilter {

    private static final StringReplaceJsonCompactor fallbackCompactor = new StringReplaceJsonCompactor();

    private final String replacement;
    private final Set<String> fields;
    private final JsonFactory factory;
    private final JsonGeneratorWrapper jsonGeneratorWrapper;

    public JacksonJsonFieldBodyFilter(final Collection<String> fieldNames,
                                      final String replacement,
                                      final JsonFactory factory,
                                      final JsonGeneratorWrapper jsonGeneratorWrapper) {
        this.fields = new HashSet<>(fieldNames); // thread safe for reading
        this.replacement = replacement;
        this.factory = factory;
        this.jsonGeneratorWrapper = jsonGeneratorWrapper;
    }

    public JacksonJsonFieldBodyFilter(final Collection<String> fieldNames, final String replacement, final JsonFactory factory) {
        this(fieldNames, replacement, factory, new DefaultJsonGeneratorWrapper());
    }

    public JacksonJsonFieldBodyFilter(final Collection<String> fieldNames, final String replacement) {
        this(fieldNames, replacement, new JsonFactory());
    }

    @Override
    public String filter(@Nullable final String contentType, final String body) {
        return ContentType.isJsonMediaType(contentType) ? filter(body) : body;
    }

    public String filter(final String body) {
        try ( final CharArrayWriter  writer = new CharArrayWriter(body.length() * 2) ){ // rough estimate of final size)

            try (final JsonParser parser = factory.createParser(ObjectReadContext.empty(), body);
                 final JsonGenerator generator = factory.createGenerator(ObjectWriteContext.empty(), writer)){

                JsonToken nextToken;
                while ((nextToken = parser.nextToken()) != null) {
                    jsonGeneratorWrapper.copyCurrentEvent(generator, parser);
                    if (nextToken == JsonToken.PROPERTY_NAME && fields.contains(parser.currentName())) {
                        nextToken = parser.nextToken();
                        generator.writeString(replacement);
                        if (!nextToken.isScalarValue()) {
                            parser.skipChildren(); // skip children
                        }
                    }
                }
            }
            return writer.toString();
        } catch (final Exception e) {
            log.trace("Unable to filter body for fields {}, compacting result. `{}`", fields, e.getMessage());
            return fallbackCompactor.compact(body);
        }
    }

}
