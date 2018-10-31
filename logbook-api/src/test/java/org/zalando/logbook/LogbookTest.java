package org.zalando.logbook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class LogbookTest {

    private final HeaderFilter headerFilter = mock(HeaderFilter.class);
    private final QueryFilter queryFilter = mock(QueryFilter.class);
    private final BodyFilter bodyFilter = mock(BodyFilter.class);
    private final RequestFilter requestFilter = mock(RequestFilter.class);
    private final ResponseFilter responseFilter = mock(ResponseFilter.class);
    private final Strategy strategy = mock(Strategy.class);
    private final Sink sink = mock(Sink.class);

    private Mockbook setUp(final int times) {
        return (Mockbook) create(times);
    }

    private Logbook create(final int times) {

        switch (times) {
            case 0:
                return Logbook.builder()
                        .strategy(strategy)
                        .sink(sink)
                        .build();
            case 1:
                return Logbook.builder()
                        .queryFilter(queryFilter)
                        .headerFilter(headerFilter)
                        .bodyFilter(bodyFilter)
                        .requestFilter(requestFilter)
                        .responseFilter(responseFilter)
                        .strategy(strategy)
                        .sink(sink)
                        .build();
            case 2:
                return Logbook.builder()
                        .queryFilter(queryFilter)
                        .queryFilter(queryFilter)
                        .headerFilter(headerFilter)
                        .headerFilter(headerFilter)
                        .bodyFilter(bodyFilter)
                        .bodyFilter(bodyFilter)
                        .requestFilter(requestFilter)
                        .requestFilter(requestFilter)
                        .responseFilter(responseFilter)
                        .responseFilter(responseFilter)
                        .strategy(strategy)
                        .sink(sink)
                        .build();
            case 3:
                return Logbook.builder()
                        .queryFilters(singleton(queryFilter))
                        .queryFilters(asList(queryFilter, queryFilter))
                        .headerFilters(singleton(headerFilter))
                        .headerFilters(asList(headerFilter, headerFilter))
                        .bodyFilters(singleton(bodyFilter))
                        .bodyFilters(asList(bodyFilter, bodyFilter))
                        .requestFilters(singleton(requestFilter))
                        .requestFilters(asList(requestFilter, requestFilter))
                        .responseFilters(singleton(responseFilter))
                        .responseFilters(asList(responseFilter, responseFilter))
                        .strategy(strategy)
                        .sink(sink)
                        .build();
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Test
    void shouldCreateInstance() {
        final Logbook logbook = Logbook.create();
        assertThat(logbook, is(notNullValue()));
    }

    @Test
    void shouldNotCombineQueryFilters() {
        final Mockbook unit = setUp(0);
        assertThat(unit.getQueryFilter(), is(nullValue()));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void shouldCombineQueryFilters(final int times) {
        final Mockbook unit = setUp(times);
        unit.getQueryFilter().filter("test");
        verify(queryFilter, times(times)).filter(any());
    }

    @Test
    void shouldNotCombineHeaderFilters() {
        final Mockbook unit = setUp(0);
        assertThat(unit.getHeaderFilter(), is(nullValue()));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void shouldCombineHeaderFilters(final int times) {
        final Mockbook unit = setUp(times);
        unit.getHeaderFilter().filter(singletonMap("test", singletonList("test")));
        verify(headerFilter, times(times)).filter(any());
    }

    @Test
    void shouldNotCombineBodyFilters() {
        final Mockbook unit = setUp(0);
        assertThat(unit.getHeaderFilter(), is(nullValue()));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void shouldCombineBodyFilters(final int times) {
        final Mockbook unit = setUp(times);
        unit.getBodyFilter().filter("text/plain", "test");
        verify(bodyFilter, times(times)).filter(any(), any());
    }

    @Test
    void shouldNotCombineRequestFilters() {
        final Mockbook unit = setUp(0);
        assertThat(unit.getRequestFilter(), is(nullValue()));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void shouldCombineRequestFilters(final int times) {
        final Mockbook unit = setUp(times);
        unit.getRequestFilter().filter(mock(HttpRequest.class));
        verify(requestFilter, times(times)).filter(any());
    }

    @Test
    void shouldNotCombineResponseFilters() {
        final Mockbook unit = setUp(0);
        assertThat(unit.getResponseFilter(), is(nullValue()));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void shouldCombineResponseFilters(final int times) {
        final Mockbook unit = setUp(times);
        unit.getResponseFilter().filter(mock(HttpResponse.class));
        verify(responseFilter, times(times)).filter(any());
    }

    @Test
    void shouldUseBuilderToString() {
        assertThat(Logbook.builder(), hasToString(notNullValue()));
    }

}
