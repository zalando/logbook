package org.zalando.logbook;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

final class Fold {

    private Fold() {

    }

    static <T, R> R fold(
            final Collection<T> collection,
            final R seed,
            final BiFunction<R, T, R> accumulator) {

        return collection.stream()
                .reduce(seed, accumulator, throwingCombiner());
    }

    @SuppressWarnings("unchecked")
    private static <R> BinaryOperator<R> throwingCombiner() {
        return (BinaryOperator<R>) NoCombiner.NONE;
    }

    // visible for testing
    enum NoCombiner implements BinaryOperator<Object> {
        NONE;

        @Override
        public Object apply(final Object left, final Object right) {
            throw new UnsupportedOperationException();
        }
    }

}
