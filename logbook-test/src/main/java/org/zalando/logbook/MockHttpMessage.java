package org.zalando.logbook;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;


interface MockHttpMessage {

    default  <K, V> Map<K, V> firstNonNullNorEmpty(@Nullable final Map<K, V> first,
            final Map<K, V> second) {
        return first != null && !first.isEmpty() ? first : Objects.requireNonNull(second);
    }

}
