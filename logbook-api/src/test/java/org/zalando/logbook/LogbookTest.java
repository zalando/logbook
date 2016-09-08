package org.zalando.logbook;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    private RawRequestFilter rawRequestFilter;

    @Mock
    private RawResponseFilter rawResponseFilter;

    @Mock
    private HeaderFilter headerFilter;

    @Mock
    private QueryFilter queryFilter;

    @Mock
    private BodyFilter bodyFilter;

    @Mock
    private RequestFilter requestFilter;

    @Mock
    private ResponseFilter responseFilter;

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
                        .rawRequestFilter(rawRequestFilter)
                        .rawResponseFilter(rawResponseFilter)
                        .queryFilter(queryFilter)
                        .headerFilter(headerFilter)
                        .bodyFilter(bodyFilter)
                        .requestFilter(requestFilter)
                        .responseFilter(responseFilter)
                        .formatter(formatter)
                        .writer(writer)
                        .build();
                return;
            case 2:
                this.logbook = Logbook.builder()
                        .condition(predicate)
                        .rawRequestFilter(rawRequestFilter)
                        .rawRequestFilter(rawRequestFilter)
                        .rawResponseFilter(rawResponseFilter)
                        .rawResponseFilter(rawResponseFilter)
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
                        .formatter(formatter)
                        .writer(writer)
                        .build();
                return;
            case 3:
                this.logbook = Logbook.builder()
                        .condition(predicate)
                        .rawRequestFilters(singleton(rawRequestFilter))
                        .rawRequestFilters(asList(rawRequestFilter, rawRequestFilter))
                        .rawResponseFilters(singleton(rawResponseFilter))
                        .rawResponseFilters(asList(rawResponseFilter,rawResponseFilter))
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
    public void shouldCombineRawRequestFilters() {
        final Mockbook mockbook = Mockbook.class.cast(logbook);

        if (times == 0) {
            assertThat(mockbook.getRawRequestFilter(), is(nullValue()));
        } else {
            mockbook.getRawRequestFilter().filter(mock(RawHttpRequest.class));
            verify(rawRequestFilter, times(times)).filter(any());
        }
    }

    @Test
    public void shouldCombineRawResponseFilters() {
        final Mockbook mockbook = Mockbook.class.cast(logbook);

        if (times == 0) {
            assertThat(mockbook.getRawResponseFilter(), is(nullValue()));
        } else {
            mockbook.getRawResponseFilter().filter(mock(RawHttpResponse.class));
            verify(rawResponseFilter, times(times)).filter(any());
        }
    }

    @Test
    public void shouldCombineQueryFilters() {
        final Mockbook mockbook = Mockbook.class.cast(logbook);

        if (times == 0) {
            assertThat(mockbook.getQueryFilter(), is(nullValue()));
        } else {
            mockbook.getQueryFilter().filter("test");
            verify(queryFilter, times(times)).filter(any());
        }
    }

    @Test
    public void shouldCombineHeaderFilters() {
        final Mockbook mockbook = Mockbook.class.cast(logbook);

        if (times == 0) {
            assertThat(mockbook.getHeaderFilter(), is(nullValue()));
        } else {
            mockbook.getHeaderFilter().filter(singletonMap("test", singletonList("test")));
            verify(headerFilter, times(times)).filter(any());
        }
    }

    @Test
    public void shouldCombineBodyFilters() {
        final Mockbook mockbook = Mockbook.class.cast(logbook);

        if (times == 0) {
            assertThat(mockbook.getHeaderFilter(), is(nullValue()));
        } else {
            mockbook.getBodyFilter().filter("text/plain", "test");
            verify(bodyFilter, times(times)).filter(any(), any());
        }
    }

    @Test
    public void shouldCombineRequestFilters() {
        final Mockbook mockbook = Mockbook.class.cast(logbook);

        if (times == 0) {
            assertThat(mockbook.getRequestFilter(), is(nullValue()));
        } else {
            mockbook.getRequestFilter().filter(mock(HttpRequest.class));
            verify(requestFilter, times(times)).filter(any());
        }
    }

    @Test
    public void shouldCombineResponseFilters() {
        final Mockbook mockbook = Mockbook.class.cast(logbook);

        if (times == 0) {
            assertThat(mockbook.getResponseFilter(), is(nullValue()));
        } else {
            mockbook.getResponseFilter().filter(mock(HttpResponse.class));
            verify(responseFilter, times(times)).filter(any());
        }
    }

}