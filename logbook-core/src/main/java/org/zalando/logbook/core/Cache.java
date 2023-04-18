package org.zalando.logbook.core;

import lombok.AllArgsConstructor;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@AllArgsConstructor
final class Cache<T> {

    private final AtomicReference<T> cache = new AtomicReference<>();
    private final Supplier<T> supplier;

    public T get() {
        return cache.updateAndGet(cached ->
                cached == null ? supplier.get() : cached);
    }

}
