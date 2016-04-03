package org.zalando.logbook;

/*
 * #%L
 * Logbook: Core
 * %%
 * Copyright (C) 2015 - 2016 Zalando SE
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.*;

import static java.util.stream.Collectors.toSet;

/**
 * Created by clalleme on 02/04/2016.
 */
public interface Multimap<K, V> extends Map<K, Collection<V>> {

    default Collection<V> putValue(K key, V value) {
        computeIfAbsent(key, v -> new ArrayList<>()).add(value);
        return Collections.unmodifiableCollection(get(key));
    }

    /**
     * return set immutableOf entries, flattening the collection.
     * if we have: a -> (1,2), b -> (2,3), c->4, we'll get: (a, 1),(a,2),(b,2),(b,3),(c,4).
     */
    default Set<Map.Entry<K, V>> entries() {
        return entrySet().stream()
                         .flatMap(v -> v.getValue().stream().map(i -> new BasicEntry<>(v.getKey(), i)))
                         .collect(toSet());
    }

    class BasicEntry<X, Z> implements Map.Entry<X, Z> {
        final X key;
        Z value;

        protected BasicEntry(X key, Z value) {
            this.key = key;
            this.value = value;
        }

        public X getKey() {
            return key;
        }

        public Z getValue() {
            return value;
        }

        public Z setValue(Z value) {
            Objects.requireNonNull(value);

            Z oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;

            return (key == null ? e.getKey() == null : key.equals(e.getKey())) &&
                    (value == null ? e.getValue() == null : value.equals(e.getValue()));
        }

        public int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        public String toString() {
            return key.toString() + "=" + value.toString();
        }
    }
}
