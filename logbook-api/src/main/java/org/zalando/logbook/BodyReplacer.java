package org.zalando.logbook;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

@FunctionalInterface
public interface BodyReplacer<T extends BaseHttpMessage> {

    @Nullable
    String replace(final T message);

    @SafeVarargs
    static <T extends BaseHttpMessage> BodyReplacer<T> compound(final BodyReplacer<T>... replacers) {
        return message -> Arrays.stream(replacers)
                .map(replacer -> replacer.replace(message))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

}
