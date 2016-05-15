package org.zalando.logbook;

/*
 * #%L
 * Logbook: Test
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

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class MockHeadersTest {

    @Test
    public void satisfyCoverage() {
        new MockHeaders();
    }

    @Test
    public void testOf1() {
        final Map<String, List<String>> m = MockHeaders.of("x", "y");

        assertEquals(1, m.size());
        assertEquals(m.get("x"), singletonList("y"));
    }

    @Test
    public void testOf2() {
        final Map<String, List<String>> m = MockHeaders.of("x", "y", "a", "b");

        assertEquals(2, m.size());
        assertEquals(m.get("x"), singletonList("y"));
        assertEquals(m.get("a"), singletonList("b"));
    }

    @Test
    public void testOf3() {
        final Map<String, List<String>> m = MockHeaders.of("x", "y", "a", "b", "1", "2");

        assertEquals(3, m.size());
        assertEquals(m.get("x"), singletonList("y"));
        assertEquals(m.get("a"), singletonList("b"));
        assertEquals(m.get("1"), singletonList("2"));
    }
}
