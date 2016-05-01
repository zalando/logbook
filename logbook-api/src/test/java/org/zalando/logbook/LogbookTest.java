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

import org.junit.Test;

import java.util.function.Predicate;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class LogbookTest {

    @Test
    public void shouldCreateInstance() {
        final Logbook logbook = Logbook.create();
        assertThat(logbook, is(notNullValue()));
    }

    @Test
    public void shouldCreateCustomInstance() {
        @SuppressWarnings("unchecked")
        final Predicate<RawHttpRequest> predicate = mock(Predicate.class);
        final HeaderObfuscator headerObfuscator = mock(HeaderObfuscator.class);
        final QueryObfuscator queryObfuscator = mock(QueryObfuscator.class);
        final BodyObfuscator bodyObfuscator = mock(BodyObfuscator.class);
        final HttpLogFormatter formatter = mock(HttpLogFormatter.class);
        final HttpLogWriter writer = mock(HttpLogWriter.class);

        final Logbook logbook = Logbook.builder()
                .condition(predicate)
                .queryObfuscator(queryObfuscator)
                .headerObfuscator(headerObfuscator)
                .bodyObfuscator(bodyObfuscator)
                .formatter(formatter)
                .writer(writer)
                .build();

        assertThat(logbook, is(instanceOf(FakeLogbook.class)));

        final FakeLogbook fake = FakeLogbook.class.cast(logbook);

        assertThat(fake.getPredicate(), is(sameInstance(predicate)));
        assertThat(fake.getHeaderObfuscator(), is(sameInstance(headerObfuscator)));
        assertThat(fake.getQueryObfuscator(), is(sameInstance(queryObfuscator)));
        assertThat(fake.getBodyObfuscator(), is(sameInstance(bodyObfuscator)));
        assertThat(fake.getFormatter(), is(sameInstance(formatter)));
        assertThat(fake.getWriter(), is(sameInstance(writer)));
    }

}