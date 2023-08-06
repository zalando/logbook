package org.zalando.logbook.attributes;

import lombok.Getter;
import org.apiguardian.api.API;

import java.util.Collections;
import java.util.Map;

import static org.apiguardian.api.API.Status.STABLE;

@Getter
@API(status = STABLE)
public final class HttpAttributes {

    public static final HttpAttributes EMPTY = new HttpAttributes();

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
        if (!(o instanceof HttpAttributes)) return false;
        return map.equals(((HttpAttributes) o).map);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public String toString() {
        return map.toString();
    }

    public boolean isEmpty() {
        return map.isEmpty();
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
