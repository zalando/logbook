package org.zalando.logbook.json;

import lombok.AllArgsConstructor;
import lombok.With;
import org.zalando.logbook.BodyFilter;

import javax.annotation.Nullable;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static lombok.AccessLevel.PRIVATE;

@AllArgsConstructor(access = PRIVATE, staticName = "create")
final class PrimitiveJsonPropertyBodyFilter implements BodyFilter {

    /*language=RegExp*/
    private static final String BOOLEAN_PATTERN = "(?:true|false)";

    /*language=RegExp*/
    private static final String NUMBER_PATTERN =
            "(?:-?(?:0|[1-9][0-9]*)(?:\\.[0-9]+)?(?:[eE][+-]?[0-9]+)?)";

    private static final Pattern NUMBER = pattern(NUMBER_PATTERN);

    /**
     * @see <a href="https://stackoverflow.com/a/43597014/232539">Regex for quoted string with escaping quotes</a>
     */
    /*language=RegExp*/
    private static final String STRING_PATTERN = "(?:\"(.*?[^\\\\])??((\\\\\\\\)+)?+\")";

    private static final Pattern STRING = pattern(STRING_PATTERN);

    /*language=RegExp*/
    private static final String PRIMITIVE_PATTERN =
            "(?:" + BOOLEAN_PATTERN + "|" + NUMBER_PATTERN + "|" + STRING_PATTERN + ")";

    private static final Pattern PRIMITIVE = pattern(PRIMITIVE_PATTERN);

    private final Pattern pattern;

    @With(PRIVATE)
    private final Predicate<String> predicate;

    private final String replacement;

    private static Pattern pattern(final String value) {
        return compile("(?<key>\"(?<property>.*?)\"\\s*:\\s*)(" + value + "|null)");
    }

    static BodyFilter replaceString(
            final Predicate<String> predicate, final String replacement) {
        return create(STRING, predicate, quote(replacement));
    }

    static BodyFilter replaceNumber(
            final Predicate<String> predicate, final Number replacement) {
        return create(NUMBER, predicate, String.valueOf(replacement));
    }

    static BodyFilter replacePrimitive(
            final Predicate<String> predicate, final String replacement) {
        return create(PRIMITIVE, predicate, quote(replacement));
    }

    public static String quote(final String s) {
        return "\"" + s + "\"";
    }

    @Override
    public String filter(@Nullable final String contentType, final String body) {
        if (JsonMediaType.JSON.test(contentType)) {
            final Matcher matcher = pattern.matcher(body);
            final StringBuffer result = new StringBuffer(body.length());

            while (matcher.find()) {
                if (predicate.test(matcher.group("property"))) {
                    // this preserves whitespaces around properties
                    matcher.appendReplacement(result, "${key}");
                    result.append(replacement);
                } else {
                    matcher.appendReplacement(result, "$0");
                }
            }
            matcher.appendTail(result);

            return result.toString();
        }
        return body;
    }

    @Override
    public BodyFilter tryMerge(final BodyFilter next) {
        if (next instanceof PrimitiveJsonPropertyBodyFilter) {
            final PrimitiveJsonPropertyBodyFilter filter = (PrimitiveJsonPropertyBodyFilter) next;

            if (compatibleWith(filter)) {
                return withPredicate(predicate.or(filter.predicate));
            }
        }

        return null;
    }

    public boolean compatibleWith(final PrimitiveJsonPropertyBodyFilter that) {
        return pattern.equals(that.pattern)
                && replacement.equals(that.replacement);
    }

}
