package org.zalando.logbook;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.zalando.logbook.Fold.fold;

interface UpdateHttpHeaders extends HttpHeaders {

    @Override
    default HttpHeaders update(
            final String name,
            final String... values) {

        return update(name, Arrays.asList(values));
    }

    @Override
    default HttpHeaders update(
            final Map<String, List<String>> headers) {

        final HttpHeaders self = this;
        return fold(headers.entrySet(), self, (result, entry) -> {
            final String name = entry.getKey();
            final List<String> values = entry.getValue();
            return result.update(name, values);
        });
    }

}
