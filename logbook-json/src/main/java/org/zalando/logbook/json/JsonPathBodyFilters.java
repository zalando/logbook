package org.zalando.logbook.json;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apiguardian.api.API;
import org.zalando.logbook.BodyFilter;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@API(status = API.Status.EXPERIMENTAL)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonPathBodyFilters {

    private static final Configuration conf = Configuration.builder().options(Option.AS_PATH_LIST).build();

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class JsonPathBodyFilterBuilder {
        private final JsonPath jsonPath;

        public BodyFilter delete() {
            return (JsonBodyFilter) documentContext -> documentContext.delete(jsonPath).jsonString();
        }

        public BodyFilter replace(String replacement) {
            return replaceInternal(replacement);
        }

        public BodyFilter replace(Number replacement) {
            return replaceInternal(replacement);
        }

        public BodyFilter replace(Boolean replacement) {
            return replaceInternal(replacement);
        }

        public BodyFilter replace(String matchRegex, String replacementRegex) {
            Pattern pattern = Pattern.compile(matchRegex);
            return (JsonBodyFilter) documentContext -> {
                final List<String> paths = documentContext.read(jsonPath);
                final Object document = documentContext.json();
                paths.forEach(path -> {
                    final Object node = JsonPath.compile(path).read(document);
                    replaceWithRegex(node, pattern, replacementRegex)
                            .ifPresent(s -> documentContext.set(path, s));
                });
                return documentContext.jsonString();
            };
        }

        private BodyFilter replaceInternal(Object replacement) {
            return (JsonBodyFilter) documentContext -> documentContext.set(jsonPath, replacement).jsonString();
        }

        private Optional<String> replaceWithRegex(Object node, Pattern pattern, String replacementRegex) {
            String stringRepresentation = node.toString();
            Matcher matcher = pattern.matcher(stringRepresentation);
            if (matcher.find()) {
                return Optional.of(matcher.replaceAll(replacementRegex));
            }
            return Optional.empty();
        }

    }

    private interface JsonBodyFilter extends BodyFilter, Function<DocumentContext, String> {
        @Override
        default String filter(@Nullable String contentType, String body) {
            if (JsonMediaType.JSON.test(contentType)) {
                return apply(JsonPath.using(conf).parse(body));
            }
            return body;
        }
    }

    public static JsonPathBodyFilterBuilder jsonPath(String jsonPath) {
        return new JsonPathBodyFilterBuilder(JsonPath.compile(jsonPath));
    }
}
