package org.zalando.logbook;

/*
 * #%L
 * Logbook: Core
 * %%
 * Copyright (C) 2015 Zalando SE
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

import static com.google.common.collect.ImmutableMultimap.of;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.zalando.logbook.QueryParameters.NONE;
import static org.zalando.logbook.QueryParameters.render;

public final class QueryParametersTest {

    @Test
    public void shouldRenderEmptyQueryString() {
        assertThat(render(of()), is(""));
    }

    @Test
    public void shouldRenderDuplicates() {
        assertThat(render(of("foo", "bar", "foo", "baz")), is("foo=bar&foo=baz"));
    }

    @Test
    public void shouldRenderEmptyValues() {
        assertThat(render(of("foo", "")), is("foo="));
    }

    @Test
    public void shouldRenderEmptyKey() {
        assertThat(render(of("", "bar")), is("=bar"));
    }

    @Test
    public void shouldRenderMissingValue() {
        assertThat(render(of("foo", NONE)), is("foo"));
    }

    @Test
    public void shouldRenderInOrder() {
        assertThat(render(of("c", "3", "d", "4", "a", "1", "e", "5", "b", "2")), is("c=3&d=4&a=1&e=5&b=2"));
    }

}