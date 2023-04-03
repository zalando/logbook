package org.zalando.logbook.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.extern.slf4j.Slf4j;
import org.zalando.logbook.api.BodyFilter;

import javax.annotation.Nullable;
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

    private final static StringReplaceJsonCompactor fallbackCompactor = new StringReplaceJsonCompactor();

    private final String replacement;
    private final Set<String> fields;
    private final JsonFactory factory;

    public JacksonJsonFieldBodyFilter(final Collection<String> fieldNames, final String replacement, final JsonFactory factory) {
        this.fields = new HashSet<>(fieldNames); // thread safe for reading
        this.replacement = replacement;
        this.factory = factory;
    }

    public JacksonJsonFieldBodyFilter(final Collection<String> fieldNames, final String replacement) {
        this(fieldNames, replacement, new JsonFactory());
    }

    @Override
    public String filter(@Nullable final String contentType, final String body) {
        return JsonMediaType.JSON.test(contentType) ? filter(body) : body;
    }

    public String filter(final String body) {
        try {
            final JsonParser parser = factory.createParser(body);

            final CharArrayWriter writer = new CharArrayWriter(body.length() * 2); // rough estimate of final size

            final JsonGenerator generator = factory.createGenerator(writer);
            try {
                while (true) {
                    JsonToken nextToken = parser.nextToken();
                    if (nextToken == null) {
                        break;
                    }

                    generator.copyCurrentEvent(parser);
                    if (nextToken == JsonToken.FIELD_NAME && fields.contains(parser.getCurrentName())) {
                        nextToken = parser.nextToken();
                        generator.writeString(replacement);
                        if (!nextToken.isScalarValue()) {
                            parser.skipChildren(); // skip children
                        }
                    }
                }
            } finally {
                parser.close();

                generator.close();
            }

            return writer.toString();
        } catch (final Exception e) {
            log.trace("Unable to filter body for fields {}, compacting result. `{}`", fields, e.getMessage());
            return fallbackCompactor.compact(body);
        }
    }

}
