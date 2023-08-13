package org.zalando.logbook.attributes;

import lombok.experimental.Delegate;
import org.apiguardian.api.API;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@Immutable
@API(status = EXPERIMENTAL)
public final class HttpAttributes implements Map<String, Object> {

    public static final HttpAttributes EMPTY = new HttpAttributes();

    @Nonnull
    @Delegate
    private final Map<String, Object> map;

    public HttpAttributes() {
        map = Collections.emptyMap();
    }

    public HttpAttributes(final Map<String, Object> map) {
        this.map = Collections.unmodifiableMap(new HashMap<>(map));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Map)) return false;
        return map.equals(o);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public String toString() {
        return map.toString();
    }

    /**
     * Returns an immutable HttpAttributes, mapping only the specified key to the
     * specified value.
     *
     * @param key   the sole key to be stored in the returned HttpAttributes.
     * @param value the value to which the returned HttpAttributes maps {@code key}.
     * @return an immutable HttpAttributes containing only the specified key-value
     * mapping.
     */
    public static HttpAttributes of(String key, Object value) {
        Map<String, Object> m = Collections.singletonMap(key, value);
        return new HttpAttributes(m);
    }
}
