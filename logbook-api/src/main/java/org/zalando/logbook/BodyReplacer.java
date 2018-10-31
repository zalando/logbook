package org.zalando.logbook;

import org.apiguardian.api.API;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
@FunctionalInterface
public interface BodyReplacer<T extends HttpMessage> {

    @Nullable
    String replace(final T message);

    @SafeVarargs
    static <T extends HttpMessage> BodyReplacer<T> compound(final BodyReplacer<T>... replacers) {
        return message -> Arrays.stream(replacers)
                .map(replacer -> replacer.replace(message))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

}
