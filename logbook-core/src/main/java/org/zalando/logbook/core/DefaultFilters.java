package org.zalando.logbook.core;

import java.util.Collection;

import static java.util.ServiceLoader.load;
import static java.util.stream.StreamSupport.stream;

final class DefaultFilters {

    private DefaultFilters() {

    }

    static <T> Collection<T> defaultValues(final Class<T> defaultType) {
        return stream(load(defaultType).spliterator(), false).toList();
    }

}
