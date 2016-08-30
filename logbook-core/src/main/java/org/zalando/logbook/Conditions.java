package org.zalando.logbook;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;
import static org.zalando.logbook.RequestURI.Component.AUTHORITY;
import static org.zalando.logbook.RequestURI.Component.PATH;
import static org.zalando.logbook.RequestURI.Component.SCHEME;
import static org.zalando.logbook.RequestURI.reconstruct;

public final class Conditions {

    Conditions() {
        // package private so we can trick code coverage
    }

    @SafeVarargs
    public static Predicate<RawHttpRequest> exclude(final Predicate<RawHttpRequest>... predicates) {
        return exclude(Arrays.asList(predicates));
    }

    public static Predicate<RawHttpRequest> exclude(final Collection<Predicate<RawHttpRequest>> predicates) {
        return predicates.stream()
                .map(Predicate::negate)
                .reduce(Predicate::and)
                .orElse($ -> true);
    }

    public static Predicate<RawHttpRequest> requestTo(final String pattern) {
        final Predicate<String> predicate = Glob.compile(pattern);

        return pattern.startsWith("/") ?
                requestTo(RawHttpRequest::getPath, predicate) :
                requestTo(request -> reconstruct(request, SCHEME, AUTHORITY, PATH), predicate);
    }

    private static Predicate<RawHttpRequest> requestTo(final Function<RawHttpRequest, String> extractor,
            final Predicate<String> predicate) {
        return request -> predicate.test(extractor.apply(request));
    }

    // TODO(whiskeysierra): This should probably be more sophisticated, i.e. contains/compatibleWith
    public static Predicate<RawHttpRequest> contentType(final String contentType) {
        return request ->
                contentType.equals(request.getContentType());
    }

    public static Predicate<RawHttpRequest> header(final String key, final String value) {
        return request ->
                request.getHeaders().getOrDefault(key, emptyList()).contains(value);
    }

    public static Predicate<RawHttpRequest> header(final String key, final Predicate<String> predicate) {
        return request ->
                request.getHeaders().get(key).stream().anyMatch(predicate);
    }

    public static Predicate<RawHttpRequest> header(final BiPredicate<String, String> predicate) {
        return request ->
                request.getHeaders().entrySet().stream()
                        .anyMatch(e ->
                                e.getValue().stream().anyMatch(v -> predicate.test(e.getKey(), v)));
    }

}
