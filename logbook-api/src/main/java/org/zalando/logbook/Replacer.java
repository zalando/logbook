package org.zalando.logbook;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;

@FunctionalInterface
public interface Replacer<T extends BaseHttpMessage> {

    @Nullable
    String replace(final T message);

    static <T extends BaseHttpMessage> Replacer<T> replaceWith(final Predicate<T> predicate, final String replacement) {
        return message -> predicate.test(message) ? replacement : null;
    }

    @SafeVarargs
    static <T extends BaseHttpMessage> Replacer<T> compound(final Replacer<T>... replacers) {
        return message -> Arrays.stream(replacers)
                .map(replacer -> replacer.replace(message))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

}
