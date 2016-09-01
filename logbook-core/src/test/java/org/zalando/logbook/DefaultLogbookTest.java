package org.zalando.logbook;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
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
    private final HeaderObfuscator headerObfuscator = mock(HeaderObfuscator.class);
    private final QueryObfuscator queryObfuscator = mock(QueryObfuscator.class);
    private final BodyObfuscator bodyObfuscator = mock(BodyObfuscator.class);

    private final Logbook unit = Logbook.builder()
            .condition(predicate)
            .queryObfuscator(queryObfuscator)
            .headerObfuscator(headerObfuscator)
            .bodyObfuscator(bodyObfuscator)
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

    @Before
    public void defaultBehaviour() throws IOException {
        when(writer.isActive(any())).thenReturn(true);
        when(predicate.test(any())).thenReturn(true);
    }

    @Test
    public void shouldNotReturnCorrelatorIfInactiveWriter() throws IOException {
        when(writer.isActive(any())).thenReturn(false);

        final Optional<Correlator> correlator = unit.write(request);

        assertThat(correlator, hasFeature("present", Optional::isPresent, is(false)));
    }

    @Test
    public void shouldNotReturnCorrelatorIfPredicateTestsFalse() throws IOException {
        when(writer.isActive(any())).thenReturn(true);
        when(predicate.test(any())).thenReturn(false);

        final Optional<Correlator> correlator = unit.write(request);

        assertThat(correlator, hasFeature("present", Optional::isPresent, is(false)));
    }

    @Test
    public void shouldNeverRetrieveBodyIfInactiveWriter() throws IOException {
        when(writer.isActive(any())).thenReturn(false);

        unit.write(request);

        verify(request, never()).withBody();
    }

    @Test
    public void shouldObfuscateRequest() throws IOException {
        final Correlator correlator = unit.write(request).get();

        correlator.write(response);

        @SuppressWarnings("unchecked")
        final ArgumentCaptor<Precorrelation<HttpRequest>> captor = ArgumentCaptor.forClass(Precorrelation.class);
        verify(formatter).format(captor.capture());
        final Precorrelation<HttpRequest> precorrelation = captor.getValue();

        assertThat(precorrelation.getRequest(), instanceOf(ObfuscatedHttpRequest.class));
    }

    @Test
    public void shouldObfuscateResponse() throws IOException {
        final Correlator correlator = unit.write(request).get();

        correlator.write(response);

        @SuppressWarnings("unchecked")
        final ArgumentCaptor<Correlation<HttpRequest, HttpResponse>> captor = ArgumentCaptor.forClass(Correlation.class);
        verify(formatter).format(captor.capture());
        final Correlation<HttpRequest, HttpResponse> correlation = captor.getValue();

        assertThat(correlation.getRequest(), instanceOf(ObfuscatedHttpRequest.class));
        assertThat(correlation.getResponse(), instanceOf(ObfuscatedHttpResponse.class));
    }

}