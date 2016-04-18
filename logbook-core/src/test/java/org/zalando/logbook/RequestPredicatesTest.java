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
import static java.util.regex.Pattern.compile;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.zalando.logbook.RequestPredicates.contentType;
import static org.zalando.logbook.RequestPredicates.exclude;
import static org.zalando.logbook.RequestPredicates.header;
import static org.zalando.logbook.RequestPredicates.requestTo;

public final class RequestPredicatesTest {
    
    private final RawHttpRequest request = MockRawHttpRequest.builder()
            .headers(ImmutableListMultimap.of("X-Secret", "true"))
            .contentType("text/plain")
            .build();
    
    @Test
    public void excludeShouldMatchIfNoneMatches() {
        final Predicate<RawHttpRequest> unit = exclude(requestTo("/"), contentType("application/json"));

        assertThat(unit.test(request), is(true));
    }
    
    @Test
    public void excludeNotShouldMatchIfAnyMatches() {
        final Predicate<RawHttpRequest> unit = exclude(requestTo("/"), contentType("text/plain"));

        assertThat(unit.test(request), is(false));
    }
    
    @Test
    public void excludeNotShouldMatchIfAllMatches() {
        final Predicate<RawHttpRequest> unit = exclude(requestTo("http://localhost/"), contentType("text/plain"));

        assertThat(unit.test(request), is(false));
    }

    @Test
    public void excludeShouldDefaultToAlwaysTrue() {
        final Predicate<RawHttpRequest> unit = exclude();
        
        assertThat(unit.test(null), is(true));
    }

    @Test
    public void requestToShouldMatchString() {
        final Predicate<RawHttpRequest> unit = requestTo("http://localhost/");
        
        assertThat(unit.test(request), is(true));
    }

    @Test
    public void requestToShouldNotMatchString() {
        final Predicate<RawHttpRequest> unit = requestTo("/");
        
        assertThat(unit.test(request), is(false));
    }

    @Test
    public void requestToShouldMatchPattern() {
        final Predicate<RawHttpRequest> unit = requestTo(compile("https?://localhost/?.*"));

        assertThat(unit.test(request), is(true));
    }

    @Test
    public void requestToShouldNotPatternString() {
        final Predicate<RawHttpRequest> unit = requestTo(compile("https://localhost/?.*"));

        assertThat(unit.test(request), is(false));
    }

    @Test
    public void requestToShouldMatchPredicate() {
        final Predicate<RawHttpRequest> unit = requestTo(url -> url.startsWith("http"));

        assertThat(unit.test(request), is(true));
    }

    @Test
    public void requestToShoulNotdMatchPredicate() {
        final Predicate<RawHttpRequest> unit = requestTo(url -> url.startsWith("https"));

        assertThat(unit.test(request), is(false));
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