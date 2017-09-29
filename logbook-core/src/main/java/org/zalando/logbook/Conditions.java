package org.zalando.logbook;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.zalando.logbook.RequestURI.Component.AUTHORITY;
import static org.zalando.logbook.RequestURI.Component.PATH;
import static org.zalando.logbook.RequestURI.Component.SCHEME;
import static org.zalando.logbook.RequestURI.reconstruct;

public final class Conditions {

    Conditions() {
        // package private so we can trick code coverage
    }

    @SafeVarargs
    public static <T extends BaseHttpMessage> Predicate<T> exclude(final Predicate<T>... predicates) {
        return exclude(Arrays.asList(predicates));
    }

    public static <T extends BaseHttpMessage> Predicate<T> exclude(final Collection<Predicate<T>> predicates) {
        return predicates.stream()
                .map(Predicate::negate)
                .reduce(Predicate::and)
                .orElse($ -> true);
    }

    public static <T extends BaseHttpRequest> Predicate<T> requestTo(final String pattern) {
        final Predicate<String> predicate = Glob.compile(pattern);

        return pattern.startsWith("/") ?
                requestTo(BaseHttpRequest::getPath, predicate) :
                requestTo(request -> reconstruct(request, SCHEME, AUTHORITY, PATH), predicate);
    }

    private static <T extends BaseHttpRequest> Predicate<T> requestTo(final Function<BaseHttpRequest, String> extractor,
            final Predicate<String> predicate) {
        return request -> predicate.test(extractor.apply(request));
    }

    public static <T extends BaseHttpMessage> Predicate<T> contentType(final String... contentTypes) {
        final Predicate<String> query = MediaTypeQuery.compile(contentTypes);

        return message ->
                query.test(message.getContentType());
    }

    public static <T extends BaseHttpMessage> Predicate<T> header(final String key, final String value) {
        return message ->
                message.getHeaders().getOrDefault(key, emptyList()).contains(value);
    }

    public static <T extends BaseHttpMessage> Predicate<T> header(final String key, final Predicate<String> predicate) {
        return message -> ofNullable(message.getHeaders().get(key))
                .map(hv -> hv.stream().anyMatch(predicate))
                .orElse(predicate.test(null));
    }

    public static <T extends BaseHttpMessage> Predicate<T> header(final BiPredicate<String, String> predicate) {
        return message ->
                message.getHeaders().entrySet().stream()
                        .anyMatch(e ->
                                e.getValue().stream().anyMatch(v -> predicate.test(e.getKey(), v)));
    }

}
