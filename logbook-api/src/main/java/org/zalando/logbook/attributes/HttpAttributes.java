package org.zalando.logbook.attributes;

import lombok.experimental.Delegate;
import org.apiguardian.api.API;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.Map;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@Immutable
@API(status = EXPERIMENTAL)
public final class HttpAttributes implements Map<String, String> {

    public static final HttpAttributes EMPTY = new HttpAttributes();

    @Delegate
    private final Map<String, String> map;

    public HttpAttributes() {
        map = Collections.emptyMap();
    }

    public HttpAttributes(Map<String, String> map) {
        this.map = Collections.unmodifiableMap(map);
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
    public static HttpAttributes of(String key, String value) {
        Map<String, String> m = Collections.singletonMap(key, value);
        return new HttpAttributes(m);
    }
}
