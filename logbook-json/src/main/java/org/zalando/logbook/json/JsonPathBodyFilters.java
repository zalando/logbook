package org.zalando.logbook.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apiguardian.api.API;
import org.zalando.logbook.BodyFilter;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jayway.jsonpath.JsonPath.compile;
import static lombok.AccessLevel.PRIVATE;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.zalando.logbook.internal.JsonMediaType.JSON;

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
            return replace(new TextNode(replacement));
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
            return filter(context -> context.map(path, (node, config) -> node == null ? NullNode.getInstance() : new TextNode(replacementFunction.apply(node.toString()))));
        }

        public BodyFilter replace(final Pattern pattern, final String replacement) {
            return filter(context -> context.map(path, (node, config) -> {
                if (node == null) {
                    return NullNode.getInstance();
                }

                final Matcher matcher = pattern.matcher(node.toString());

                if (matcher.find()) {
                    return new TextNode(matcher.replaceAll(replacement));
                } else {
                    return node;
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
                        .jsonProvider(new JacksonJsonNodeJsonProvider())
                        .mappingProvider(new JacksonMappingProvider())
                        .options(Option.SUPPRESS_EXCEPTIONS)
                        .options(Option.ALWAYS_RETURN_LIST)
                        .build());

        private final Operation operation;

        @Override
        public String filter(
                @Nullable final String contentType, final String body) {

            if (body.isEmpty() || !JSON.test(contentType)) {
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

            for (final Operation operation : operations) {
                result = operation.filter(result);
            }

            return result;
        }
    }

    public static JsonPathBodyFilterBuilder jsonPath(final String jsonPath) {
        return new JsonPathBodyFilterBuilder(compile(jsonPath));
    }

}
