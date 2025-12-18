package org.zalando.logbook.json;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ParseContext;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apiguardian.api.API;
import org.zalando.logbook.BodyFilter;
import org.zalando.logbook.ContentType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.BooleanNode;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.StringNode;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jayway.jsonpath.JsonPath.compile;
import static lombok.AccessLevel.PRIVATE;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
@Slf4j
@NoArgsConstructor(access = PRIVATE)
public final class JsonPathBodyFilters {

    @RequiredArgsConstructor(access = PRIVATE)
    public static final class JsonPathBodyFilterBuilder {

        private final JsonPath path;

        public BodyFilter delete() {
            return filter(context -> context.delete(path));
        }

        public BodyFilter replace(final String replacement) {
            return replace(new StringNode(replacement));
        }

        public BodyFilter replace(final Boolean replacement) {
            return replace(BooleanNode.valueOf(replacement));
        }

        public BodyFilter replace(final Double replacement) {
            return replace(new DoubleNode(replacement));
        }

        public BodyFilter replace(final JsonNode replacement) {
            return filter(context -> context.set(path, replacement));
        }

        public BodyFilter replace(final UnaryOperator<String> replacementFunction) {
            return filter(context -> context.map(path, (node, config) -> {
                Object unwrapped = context.configuration().jsonProvider().unwrap(node);
                return unwrapped == null ?
                        NullNode.getInstance() : new StringNode(replacementFunction.apply(unwrapped.toString()));
            }));
        }

        public BodyFilter replace(final Pattern pattern, final String replacement) {
            return filter(context -> context.map(path, (node, config) -> {
                Object unwrapped = context.configuration().jsonProvider().unwrap(node);

                if (unwrapped == null) {
                    return NullNode.getInstance();
                }

                final Matcher matcher = pattern.matcher(unwrapped.toString());

                if (matcher.find()) {
                    return new StringNode(matcher.replaceAll(replacement));
                } else {
                    return unwrapped;
                }
            }));
        }

    }

    private static JsonPathBodyFilter filter(final Operation operation) {
        return new JsonPathBodyFilter(operation);
    }

    @AllArgsConstructor
    private static class JsonPathBodyFilter implements BodyFilter {

        private static final ParseContext CONTEXT = JsonPath.using(
                Configuration.builder()
                        .jsonProvider(new LogbookJacksonJsonProvider())
                        .mappingProvider(new LogbookJacksonMappingProvider())
                        .options(Option.SUPPRESS_EXCEPTIONS)
                        .options(Option.ALWAYS_RETURN_LIST)
                        .build());

        private final Operation operation;

        @Override
        public String filter(
                @Nullable final String contentType, final String body) {

            if (body.isEmpty() || !ContentType.isJsonMediaType(contentType)) {
                return body;
            }

            try {
                final DocumentContext original = CONTEXT.parse(body);
                return operation.filter(original).jsonString();
            } catch (Exception e) {
                log.trace("The body could not be filtered, the following exception {} has been thrown", e.getClass());
                return body;
            }

        }

        @Nullable
        @Override
        public BodyFilter tryMerge(final BodyFilter next) {
            if (next instanceof JsonPathBodyFilter) {
                final JsonPathBodyFilter filter = (JsonPathBodyFilter) next;
                return new JsonPathBodyFilter(
                        Operation.composite(operation, filter.operation));
            }
            return BodyFilter.super.tryMerge(next);
        }

    }

    @FunctionalInterface
    private interface Operation {
        DocumentContext filter(DocumentContext context);

        static Operation composite(final Operation... operations) {
            return composite(Arrays.asList(operations));
        }

        static Operation composite(final Collection<Operation> operations) {
            return new CompositeOperation(operations);
        }
    }

    @AllArgsConstructor
    private static final class CompositeOperation implements Operation {

        private final Collection<Operation> operations;

        @Override
        public DocumentContext filter(final DocumentContext context) {
            DocumentContext result = context;

            final List<String> filterExceptions = new ArrayList<>();
            for (final Operation operation : operations) {
                try {
                    result = operation.filter(result);
                } catch (Exception e) {
                    filterExceptions.add(String.format("Exception class: %s. Message: %s", e.getClass().getName(), e.getMessage()));
                }
            }

            if (!filterExceptions.isEmpty()) {
                log.trace("JsonPathBodyFilter filter operation(s) could not complete, the following exception(s) have been thrown: " +
                        String.join(";", filterExceptions));
            }

            return result;
        }
    }

    public static JsonPathBodyFilterBuilder jsonPath(final String jsonPath) {
        return new JsonPathBodyFilterBuilder(compile(jsonPath));
    }

}
