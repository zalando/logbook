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

import com.google.common.collect.ImmutableListMultimap;
import org.junit.Test;

import java.util.function.Predicate;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.zalando.logbook.Conditions.contentType;
import static org.zalando.logbook.Conditions.exclude;
import static org.zalando.logbook.Conditions.header;
import static org.zalando.logbook.Conditions.requestTo;

public final class ConditionsTest {
    
    private final RawHttpRequest request = MockRawHttpRequest.request()
            .headers(ImmutableListMultimap.of("X-Secret", "true"))
            .contentType("text/plain")
            .build();

    @Test
    public void excludeShouldMatchIfNoneMatches() {
        final Predicate<RawHttpRequest> unit = exclude(requestTo("/admin"), contentType("application/json"));

        assertThat(unit.test(request), is(true));
    }
    
    @Test
    public void excludeNotShouldMatchIfAnyMatches() {
        final Predicate<RawHttpRequest> unit = exclude(requestTo("/admin"), contentType("text/plain"));

        assertThat(unit.test(request), is(false));
    }
    
    @Test
    public void excludeNotShouldMatchIfAllMatches() {
        final Predicate<RawHttpRequest> unit = exclude(requestTo("/"), contentType("text/plain"));

        assertThat(unit.test(request), is(false));
    }

    @Test
    public void excludeShouldDefaultToAlwaysTrue() {
        final Predicate<RawHttpRequest> unit = exclude();
        
        assertThat(unit.test(null), is(true));
    }

    @Test
    public void requestToShouldMatchURI() {
        final Predicate<RawHttpRequest> unit = requestTo("http://localhost/");

        assertThat(unit.test(request), is(true));
    }

    @Test
    public void requestToShouldNotMatchURIPattern() {
        final Predicate<RawHttpRequest> unit = requestTo("http://192.168.0.1/*");

        assertThat(unit.test(request), is(false));
    }

    @Test
    public void requestToShouldIgnoreQueryParameters() {
        final Predicate<RawHttpRequest> unit = requestTo("http://localhost/*");

        final MockRawHttpRequest request = MockRawHttpRequest.request()
                .query("location=/bar")
                .build();

        assertThat(unit.test(request), is(true));
    }

    @Test
    public void requestToShouldMatchPath() {
        final Predicate<RawHttpRequest> unit = requestTo("/");

        assertThat(unit.test(request), is(true));
    }

    @Test
    public void contentTypeShouldMatch() {
        final Predicate<RawHttpRequest> unit = contentType("text/plain");

        assertThat(unit.test(request), is(true));
    }

    @Test
    public void contentTypeShouldNotMatch() {
        final Predicate<RawHttpRequest> unit = contentType("application/json");

        assertThat(unit.test(request), is(false));
    }

    @Test
    public void headerShouldMatchNameAndValue() {
        final Predicate<RawHttpRequest> unit = header("X-Secret", "true");

        assertThat(unit.test(request), is(true));
    }

    @Test
    public void headerShouldNotMatchNameAndValue() {
        final Predicate<RawHttpRequest> unit = header("X-Secret", "false");

        assertThat(unit.test(request), is(false));
    }

    @Test
    public void headerShouldMatchNameAndValuePredicate() {
        final Predicate<RawHttpRequest> unit = header("X-Secret", newHashSet("true", "1")::contains);

        assertThat(unit.test(request), is(true));
    }

    @Test
    public void headerShouldNotMatchNameAndValuePredicate() {
        final Predicate<RawHttpRequest> unit = header("X-Secret", newHashSet("yes", "1")::contains);

        assertThat(unit.test(request), is(false));
    }

    @Test
    public void headerShouldMatchPredicate() {
        final Predicate<RawHttpRequest> unit = header((name, value) -> 
                name.equalsIgnoreCase("X-Secret") && value.equalsIgnoreCase("true"));

        assertThat(unit.test(request), is(true));
    }

    @Test
    public void headerShouldNotMatchPredicate() {
        final Predicate<RawHttpRequest> unit = header((name, value) -> 
                name.equalsIgnoreCase("X-Secret") && value.equalsIgnoreCase("false"));

        assertThat(unit.test(request), is(false));
    }

}