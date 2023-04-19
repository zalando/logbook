package org.zalando.logbook;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;

interface DeleteHttpHeaders extends HttpHeaders {

    @Override
    default HttpHeaders delete(final String... names) {
        return delete(Arrays.asList(names));
    }

    @Override
    default HttpHeaders delete(
            final BiPredicate<String, List<String>> predicate) {
        return apply(predicate, (name, previous) -> null);
    }

}
