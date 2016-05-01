package org.zalando.logbook;

/*
 * #%L
 * Logbook: API
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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(Parameterized.class)
public class LogbookTest {

    @Rule
    public final MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    private Predicate<RawHttpRequest> predicate;

    @Mock
    private HeaderObfuscator headerObfuscator;

    @Mock
    private QueryObfuscator queryObfuscator;

    @Mock
    private BodyObfuscator bodyObfuscator;

    @Mock
    private HttpLogFormatter formatter;

    @Mock
    private HttpLogWriter writer;

    private final int times;

    private Logbook logbook;

    public LogbookTest(final int times) {
        this.times = times;
    }

    @Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return asList(new Object[][]{{0}, {1}, {2}, {3}});
    }

    @Before
    public void setUp() throws Exception {
        switch (times) {
            case 0:
                this.logbook = Logbook.builder()
                        .condition(predicate)
                        .formatter(formatter)
                        .writer(writer)
                        .build();
                return;
            case 1:
                this.logbook = Logbook.builder()
                        .condition(predicate)
                        .queryObfuscator(queryObfuscator)
                        .headerObfuscator(headerObfuscator)
                        .bodyObfuscator(bodyObfuscator)
                        .formatter(formatter)
                        .writer(writer)
                        .build();
                return;
            case 2:
                this.logbook = Logbook.builder()
                        .condition(predicate)
                        .queryObfuscator(queryObfuscator)
                        .queryObfuscator(queryObfuscator)
                        .headerObfuscator(headerObfuscator)
                        .headerObfuscator(headerObfuscator)
                        .bodyObfuscator(bodyObfuscator)
                        .bodyObfuscator(bodyObfuscator)
                        .formatter(formatter)
                        .writer(writer)
                        .build();
                return;
            case 3:
                this.logbook = Logbook.builder()
                        .condition(predicate)
                        .queryObfuscators(singleton(queryObfuscator))
                        .queryObfuscators(asList(queryObfuscator, queryObfuscator))
                        .headerObfuscators(singleton(headerObfuscator))
                        .headerObfuscators(asList(headerObfuscator, headerObfuscator))
                        .bodyObfuscators(singleton(bodyObfuscator))
                        .bodyObfuscators(asList(bodyObfuscator, bodyObfuscator))
                        .formatter(formatter)
                        .writer(writer)
                        .build();
        }
    }

    @Test
    public void shouldCreateInstance() {
        final Logbook logbook = Logbook.create();
        assertThat(logbook, is(notNullValue()));
    }

    @Test
    public void shouldCreateCustomInstance() {
        assertThat(logbook, is(instanceOf(Mockbook.class)));
    }

    @Test
    public void shouldCombineQueryObfuscators() {
        final Mockbook mockbook = Mockbook.class.cast(logbook);

        mockbook.getQueryObfuscator().obfuscate("test");

        verify(queryObfuscator, times(times)).obfuscate(any());
    }

    @Test
    public void shouldCombineHeaderObfuscators() {
        final Mockbook mockbook = Mockbook.class.cast(logbook);

        mockbook.getHeaderObfuscator().obfuscate("test", "test");

        verify(headerObfuscator, times(times)).obfuscate(any(), any());
    }

    @Test
    public void shouldCombineBodyObfuscators() {
        final Mockbook mockbook = Mockbook.class.cast(logbook);

        mockbook.getBodyObfuscator().obfuscate("text/plain", "test");

        verify(bodyObfuscator, times(times)).obfuscate(any(), any());
    }

}