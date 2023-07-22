package org.zalando.logbook.attributes;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apiguardian.api.API;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpAttributes implements Map<String, Object> {

    private final Map<String, Object> map;

    public static final HttpAttributes EMPTY = new HttpAttributes(Collections.emptyMap());

    public HttpAttributes() {
        map = new ConcurrentHashMap<>();
    }

    public static HttpAttributes withMap(Map<String, Object> map) {
        return new HttpAttributes(new ConcurrentHashMap<>(map));
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return map.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return map.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(@Nonnull Map<? extends String, ?> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    @Nonnull
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    @Nonnull
    public Collection<Object> values() {
        return map.values();
    }

    @Override
    @Nonnull
    public Set<Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HttpAttributes)) return false;
        return map.equals(o);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    public HttpAttributes fluentPut(String key, Object value) {
        put(key, value);
        return this;
    }

    public HttpAttributes fluentPutAll(Map<? extends String, ?> m) {
        putAll(m);
        return this;
    }

    /**
     * @return An immutable instance of HttpAttributes with no key-value pairs
     */
    public static HttpAttributes of() {
        return EMPTY;
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
