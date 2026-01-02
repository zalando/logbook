package org.zalando.logbook;

import java.util.List;
import java.util.function.BiPredicate;

interface DeleteHttpHeaders extends HttpHeaders {

    @Override
    default HttpHeaders delete(final String... names) {
        return delete(List.of(names));
    }

    @Override
    default HttpHeaders delete(
            final BiPredicate<String, List<String>> predicate) {
        return apply(predicate, (name, previous) -> null);
    }

}
