package org.zalando.logbook;

import org.apiguardian.api.API;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;
import static org.apiguardian.api.API.Status.STABLE;
import static org.zalando.logbook.RequestURI.Component.AUTHORITY;
import static org.zalando.logbook.RequestURI.Component.PATH;
import static org.zalando.logbook.RequestURI.Component.SCHEME;
import static org.zalando.logbook.RequestURI.reconstruct;

@API(status = STABLE)
public final class Conditions {

    private Conditions() {

    }

    @SafeVarargs
    public static <T extends HttpMessage> Predicate<T> exclude(final Predicate<T>... predicates) {
        return exclude(Arrays.asList(predicates));
    }

    public static <T extends HttpMessage> Predicate<T> exclude(final Collection<Predicate<T>> predicates) {
        return predicates.stream()
                .map(Predicate::negate)
                .reduce(Predicate::and)
                .orElse($ -> true);
    }

    public static <T extends HttpRequest> Predicate<T> requestTo(final String pattern) {
        final Predicate<String> predicate = Glob.compile(pattern);

        return pattern.startsWith("/") ?
                requestTo(HttpRequest::getPath, predicate) :
                requestTo(request -> reconstruct(request, SCHEME, AUTHORITY, PATH), predicate);
    }

    private static <T extends HttpRequest> Predicate<T> requestTo(final Function<HttpRequest, String> extractor,
            final Predicate<String> predicate) {
        return request -> predicate.test(extractor.apply(request));
    }

    public static <T extends HttpMessage> Predicate<T> contentType(final String... contentTypes) {
        final Predicate<String> query = MediaTypeQuery.compile(contentTypes);

        return message ->
                query.test(message.getContentType());
    }

    public static <T extends HttpMessage> Predicate<T> withoutContentType() {
        return message -> message.getContentType() == null;
    }

    public static <T extends HttpMessage> Predicate<T> header(final String key, final String value) {
        return message ->
                message.getHeaders().getOrDefault(key, emptyList()).contains(value);
    }

    public static <T extends HttpMessage> Predicate<T> header(final String key, final Predicate<String> predicate) {
        return message ->
                message.getHeaders().getOrDefault(key, emptyList()).stream().anyMatch(predicate);
    }

    public static <T extends HttpMessage> Predicate<T> header(final BiPredicate<String, String> predicate) {
        return message ->
                message.getHeaders().entrySet().stream()
                        .anyMatch(e ->
                                e.getValue().stream().anyMatch(v -> predicate.test(e.getKey(), v)));
    }

}
