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
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.zalando.logbook.QueryParameters.parse;

public final class QueryParametersTest {

    @Test
    public void nullStringShouldBecomeEmpty() {
        assertThat(parse(null).entries(), is(empty()));
    }

    @Test
    public void emptyStringShouldBecomeEmpty() {
        assertThat(parse("").entries(), is(empty()));
    }

    @Test
    public void shouldDecodeDuplicateParameters() {
        assertThat(parse("foo=bar&foo=baz").get("foo"), contains("bar", "baz"));
    }

    // TODO test decode/encode

}