package org.zalando.logbook;

import java.util.List;
import java.util.ServiceLoader;

import static java.util.ServiceLoader.load;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

final class DefaultFilters {

    private DefaultFilters() {

    }

    static <T, D extends T> List<T> defaultValues(final Class<D> defaultType) {
        final ServiceLoader<D> loader = load(defaultType);
        return stream(loader.spliterator(), false).collect(toList());
    }

}
