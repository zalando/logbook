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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

/**
 * Created by clalleme on 04/04/2016.
 */
public class Multimaps {
    public static <K, V> Multimap<K, V> immutableOf() {
        return new BasicMultimap<>();
    }

    public static <K, V> Multimap<K, V> immutableOf(K x, V y) {
        final Multimap<K, V> map = immutableOf();
        map.computeIfAbsent(x, k -> new ArrayList<>()).add(y);
        return map;
    }

    public static <K, V> Multimap<K, V> immutableOf(K x1, V y1, K x2, V y2) {
        final Multimap<K, V> map = immutableOf();
        map.computeIfAbsent(x1, k -> new ArrayList<>()).add(y1);
        map.computeIfAbsent(x2, k -> new ArrayList<>()).add(y2);
        return map;
    }

    public static Multimap<String, String> transformEntries(final Multimap<String, String> map, final Obfuscator obfuscator) {
        Multimap<String, String> result = immutableOf();
        map.forEach((k, v) -> {
            result.put(k, v.stream().map(i -> obfuscator.obfuscate(k, i)).collect(Collectors.toCollection(ArrayList::new)));
        });
        return result;
    }

    static class BasicMultimap<K, V> extends LinkedHashMap<K, Collection<V>> implements Multimap<K, V> {

    }
}
