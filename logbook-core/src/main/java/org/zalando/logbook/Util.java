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

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by clalleme on 03/04/2016.
 */
public final class Util {
    Util() {
        super();
    }

    public static <K, V> Multimap<K, V> of() {
        return new BasicMultimap<>();
    }
    
    
    public static <K, V> Multimap<K, V> immutableOf(K x, V y) {
        final Multimap<K, V> map = of();
        map.computeIfAbsent(x, k -> new ArrayList<>()).add(y);
        return map;
    }
    
    public static <K, V> Multimap<K, V> immutableOf(K x1, V y1, K x2, V y2) {
        final Multimap<K, V> map = of();
        map.computeIfAbsent(x1, k -> new ArrayList<>()).add(y1);
        map.computeIfAbsent(x2, k -> new ArrayList<>()).add(y2);
        return map;
    }
    
    public static Multimap<String, String> transformEntries(Multimap<String, String> map, final Obfuscator obfuscator) {
        Multimap<String, String> result = of();
        map.forEach((k, v) -> {
            result.put(k, v.stream().map(i -> obfuscator.obfuscate(k, i)).collect(Collectors.toCollection(ArrayList::new)));
        });
        return result;
    }

    public static byte[] toByteArray(InputStream is) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[0xFFFF];
            for (int len; (len = is.read(buffer)) != -1; )
                os.write(buffer, 0, len);
            os.flush();
            return os.toByteArray();
        }
    }
    
    @SuppressWarnings({"varargs"})
    @SafeVarargs
    public static <N> N firstNonNull(final N... values) {
        return Arrays.stream(values).filter(Objects::nonNull).findFirst().get();
    }

    public static void checkArgument(boolean condition, String errorMsg, Object... errorArgs) {
        if (!condition)
            throw new IllegalArgumentException(String.format(errorMsg, errorArgs));
    }

    public static void copy(final InputStream src, final OutputStream dest) throws IOException {
        final Reader reader = new InputStreamReader(src);
        final Writer writer = new OutputStreamWriter(dest);
        copy(reader, writer);
        writer.flush();

    }

    public static int copy(Reader input, Writer output) throws IOException {
        char[] buffer = new char[1024];
        int count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
