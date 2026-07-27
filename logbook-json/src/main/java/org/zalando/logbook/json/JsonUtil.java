package org.zalando.logbook.json;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.core.ObjectReadContext;

/**
 * Internal utility for JSON body validation.
 * Used by JsonHttpLogFormatter and FastJsonHttpLogFormatter.
 * Intentionally lightweight — uses streaming parser, no in-memory tree.
 */
final class JsonUtil {

    private static final JsonFactory FACTORY = new JsonFactory();

    private JsonUtil() {}

    /**
     * Fast check: looks at first and last non-whitespace character.
     * Rejects obvious non-JSON content (ciphertext, binary, plain text)
     * before attempting full streaming parse.
     */
    static boolean looksLikeJson(final String body) {
        if (body == null || body.isEmpty()) {
            return false;
        }
        final String trimmed = body.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        final char first = trimmed.charAt(0);
        final char last = trimmed.charAt(trimmed.length() - 1);

        // Valid JSON starts with: { [ " digit - t(rue) f(alse) n(ull)
        final boolean validStart = first == '{' || first == '['
                || first == '"'
                || (first >= '0' && first <= '9') || first == '-'
                || first == 't' || first == 'f' || first == 'n';

        // Valid JSON ends with: } ] " digit e(true/false) l(null) n
        final boolean validEnd = last == '}' || last == ']'
                || last == '"'
                || (last >= '0' && last <= '9')
                || last == 'e' || last == 'l' || last == 'n';

        return validStart && validEnd;
    }

    /**
     * Full streaming validation — only called if looksLikeJson() passes.
     * Drains all tokens without building an in-memory tree.
     */
    static boolean isValidJson(final String body, final tools.jackson.databind.json.JsonMapper mapper) {
        try (JsonParser parser = mapper.createParser(body)) {
            while (parser.nextToken() != null) {
                // drain tokens — exception thrown if invalid
            }
            return true;
        } catch (final Exception e) {
            return false;
        }
    }
}
