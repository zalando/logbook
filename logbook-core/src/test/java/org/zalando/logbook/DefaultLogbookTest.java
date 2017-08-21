package org.zalando.logbook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public final class DefaultLogbookTest {

    private final HttpLogFormatter formatter = mock(HttpLogFormatter.class);
    private final HttpLogWriter writer = mock(HttpLogWriter.class);
    @SuppressWarnings("unchecked")
    private final Predicate<RawHttpRequest> predicate = mock(Predicate.class);
    private final RawRequestFilter rawRequestFilter = mock(RawRequestFilter.class);
    private final RawResponseFilter rawResponseFilter = mock(RawResponseFilter.class);
    private final HeaderFilter headerFilter = mock(HeaderFilter.class);
    private final QueryFilter queryFilter = mock(QueryFilter.class);
    private final BodyFilter bodyFilter = mock(BodyFilter.class);

    private final Logbook unit = Logbook.builder()
            .condition(predicate)
            .rawRequestFilter(rawRequestFilter)
            .rawResponseFilter(rawResponseFilter)
            .queryFilter(queryFilter)
            .headerFilter(headerFilter)
            .bodyFilter(bodyFilter)
            .formatter(formatter)
            .writer(writer)
            .build();

    private final RawHttpRequest request = mock(RawHttpRequest.class, withSettings().
            defaultAnswer(delegateTo(MockRawHttpRequest.create())));

    private final RawHttpResponse response = MockRawHttpResponse.create();

    private static Answer delegateTo(final Object delegate) {
        return invocation ->
                invocation.getMethod().invoke(delegate, invocation.getArguments());
    }

    @BeforeEach
    public void defaultBehaviour() throws IOException {
        when(writer.isActive(any())).thenReturn(true);
        when(predicate.test(any())).thenReturn(true);
        when(rawRequestFilter.filter(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(rawResponseFilter.filter(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldNotReturnCorrelatorIfInactiveWriter() throws IOException {
        when(writer.isActive(any())).thenReturn(false);

        final Optional<Correlator> correlator = unit.write(request);

        assertThat(correlator, hasFeature("present", Optional::isPresent, is(false)));
    }

    @Test
    void shouldNotReturnCorrelatorIfPredicateTestsFalse() throws IOException {
        when(writer.isActive(any())).thenReturn(true);
        when(predicate.test(any())).thenReturn(false);

        final Optional<Correlator> correlator = unit.write(request);

        assertThat(correlator, hasFeature("present", Optional::isPresent, is(false)));
    }

    @Test
    void shouldNeverRetrieveBodyIfInactiveWriter() throws IOException {
        when(writer.isActive(any())).thenReturn(false);

        unit.write(request);

        verify(request, never()).withBody();
    }

    @Test
    void shouldFilterRawRequest() throws IOException {
        unit.write(request);

        verify(rawRequestFilter).filter(request);
    }

    @Test
    void shouldFilterRawResponse() throws IOException {
        unit.write(request).get().write(response);

        verify(rawResponseFilter).filter(response);
    }

    @Test
    void shouldFilterRequest() throws IOException {
        final Correlator correlator = unit.write(request).get();

        correlator.write(response);

        @SuppressWarnings("unchecked") final ArgumentCaptor<Precorrelation<HttpRequest>> captor = ArgumentCaptor.forClass(
                Precorrelation.class);
        verify(formatter).format(captor.capture());
        final Precorrelation<HttpRequest> precorrelation = captor.getValue();

        assertThat(precorrelation.getRequest(), instanceOf(FilteredHttpRequest.class));
    }

    @Test
    void shouldFilterResponse() throws IOException {
        final Correlator correlator = unit.write(request).get();

        correlator.write(response);

        @SuppressWarnings("unchecked") final ArgumentCaptor<Correlation<HttpRequest, HttpResponse>> captor = ArgumentCaptor.forClass(
                Correlation.class);
        verify(formatter).format(captor.capture());
        final Correlation<HttpRequest, HttpResponse> correlation = captor.getValue();

        assertThat(correlation.getRequest(), instanceOf(FilteredHttpRequest.class));
        assertThat(correlation.getResponse(), instanceOf(FilteredHttpResponse.class));
    }

}
