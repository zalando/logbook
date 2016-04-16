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

import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;

public class BasicMultimapTest {
    @Test()
    public void shouldHaveBasicMultimap() throws Exception {
        Multimap<String, String> map = Multimaps.immutableOf();
        assertTrue(map != null);
        map.put("Foo", Arrays.asList("Bar"));
        assertThat(map.get("Foo"), Matchers.contains("Bar"));
    }

    @Test()
    public void shouldHaveSetterWorkingForEntry() throws Exception {
        Multimap.BasicEntry<String, String> entry = new Multimap.BasicEntry<>("Foo", "Bar");
        assertThat(entry.getKey(), Matchers.is("Foo"));
        assertThat(entry.getValue(), Matchers.is("Bar"));
        entry.setValue("FOO");
    }

    @Test(expected = NullPointerException.class)
    public void shouldNotAcceptNullValue() throws Exception {
        Multimap.BasicEntry<String, String> entry = new Multimap.BasicEntry<>("Foo", "Bar");
        entry.setValue(null);
    }

    @Test()
    public void shouldHaveEquals() throws Exception {
        Multimap.BasicEntry<String, String> entry = new Multimap.BasicEntry<>("Foo", "Bar");
        assertFalse(entry.equals("Foo"));
        Multimap.BasicEntry<String, String> entry2 = new Multimap.BasicEntry<>(null, null);
        assertFalse(entry.equals(entry2));
        assertFalse(entry2.equals(entry));
        entry2 = new Multimap.BasicEntry<>("Foo", null);
        assertFalse(entry.equals(entry2));
        assertFalse(entry2.equals(entry));
        entry2 = new Multimap.BasicEntry<>(null, "Bar");
        assertFalse(entry.equals(entry2));
        assertFalse(entry2.equals(entry));
        entry2 = new Multimap.BasicEntry<>("Foo", "Bar");
        assertTrue(entry.equals(entry2));

        entry = new Multimap.BasicEntry<>(null, null);
        entry2 = new Multimap.BasicEntry<>(null, null);
        assertTrue(entry.equals(entry2));


    }

    @Test()
    public void shouldHaveToString() throws Exception {
        Multimap.BasicEntry<String, String> entry = new Multimap.BasicEntry<>("Foo", "Bar");
        assertThat(entry.toString(), Matchers.notNullValue());
        assertThat(entry.toString(), Matchers.allOf(containsString("Foo"), containsString("Bar")));
        entry.setValue("FOO");
    }

}
