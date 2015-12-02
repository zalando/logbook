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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.zalando.logbook.QueryParameters.parse;

public final class QueryParametersTest {

    @Test
    public void shouldParseNull() {
        assertThat(parse(null).entries(), is(empty()));
    }

    @Test
    public void shouldParseEmpty() {
        assertThat(parse("").entries(), is(empty()));
    }

    @Test
    public void shouldParseDuplicates() {
        assertThat(parse("foo=bar&foo=baz").get("foo"), contains("bar", "baz"));
    }

    @Test
    public void shouldParseEmptyValues() {
        assertThat(parse("foo=").get("foo"), contains(""));
    }

    @Test
    public void shouldParseMissingKey() {
        assertThat(parse("=bar").get(""), contains("bar"));
    }

    @Test
    public void shouldParseMissingValue() {
        assertThat(parse("foo").get("foo"), contains((String) null));
    }

    @Test
    public void shouldParseInOrder() {
        assertThat(parse("c=3&d=4&a=1&e=5&b=2").keySet(), contains("c", "d", "a", "e", "b"));
    }

    @Test
    public void shouldRenderEmptyQueryString() {
        assertThat(parse(""), hasToString(""));
    }

    @Test
    public void shouldRenderDuplicates() {
        assertThat(parse("foo=bar&foo=baz"), hasToString("foo=bar&foo=baz"));
    }

    @Test
    public void shouldRenderEmptyValues() {
        assertThat(parse("foo="), hasToString("foo="));
    }

    @Test
    public void shouldRenderMissingKey() {
        assertThat(parse("=bar"), hasToString("=bar"));
    }

    @Test
    public void shouldRenderMissingValue() {
        assertThat(parse("foo"), hasToString("foo"));
    }

    @Test
    public void shouldRenderInOrder() {
        assertThat(parse("c=3&d=4&a=1&e=5&b=2"), hasToString("c=3&d=4&a=1&e=5&b=2"));
    }

    // TODO test decode/encode

}