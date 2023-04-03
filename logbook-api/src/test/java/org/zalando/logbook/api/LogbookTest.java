package org.zalando.logbook.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class LogbookTest {

    private final HeaderFilter headerFilter = mock(HeaderFilter.class);
    private final QueryFilter queryFilter = mock(QueryFilter.class);
    private final PathFilter pathFilter = mock(PathFilter.class);
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
                        .pathFilter(pathFilter)
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
                        .pathFilter(pathFilter)
                        .pathFilter(pathFilter)
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
                        .pathFilters(singleton(pathFilter))
                        .pathFilters(asList(pathFilter, pathFilter))
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
        assertThat(logbook).isNotNull();
    }

    @Test
    void shouldNotCombineQueryFilters() {
        final Mockbook unit = setUp(0);
        assertThat(unit.getQueryFilter()).isNull();
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
        assertThat(unit.getHeaderFilter()).isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void shouldCombineHeaderFilters(final int times) {
        final Mockbook unit = setUp(times);
        unit.getHeaderFilter().filter(HttpHeaders.of("test", "test"));
        verify(headerFilter, times(times)).filter(any());
    }

    @Test
    void shouldNotCombineBodyFilters() {
        final Mockbook unit = setUp(0);
        assertThat(unit.getHeaderFilter()).isNull();
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
        assertThat(unit.getRequestFilter()).isNull();
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
        assertThat(unit.getResponseFilter()).isNull();
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
        assertThat(Logbook.builder()).asString().isNotNull();
    }

}
