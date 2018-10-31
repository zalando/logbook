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

    @SuppressWarnings("unchecked")
    private final Predicate<HttpRequest> predicate = mock(Predicate.class);
    private final HeaderFilter headerFilter = mock(HeaderFilter.class);
    private final QueryFilter queryFilter = mock(QueryFilter.class);
    private final BodyFilter bodyFilter = mock(BodyFilter.class);
    private final Sink sink = mock(Sink.class);

    private final Logbook unit = Logbook.builder()
            .condition(predicate)
            .queryFilter(queryFilter)
            .headerFilter(headerFilter)
            .bodyFilter(bodyFilter)
            .strategy(new DefaultStrategy())
            .sink(sink)
            .build();

    private final HttpRequest request = mock(HttpRequest.class, withSettings().
            defaultAnswer(delegateTo(MockHttpRequest.create())));

    private final HttpResponse response = MockHttpResponse.create();

    private static Answer delegateTo(final Object delegate) {
        return invocation ->
                invocation.getMethod().invoke(delegate, invocation.getArguments());
    }

    @BeforeEach
    public void defaultBehaviour() {
        when(sink.isActive()).thenReturn(true);
        when(predicate.test(any())).thenReturn(true);
    }

    @Test
    void shouldNotWriteIfSinkInactive() throws IOException {
        when(sink.isActive()).thenReturn(false);

        unit.process(request).write().process(response).write();

        verify(sink, never()).write(any(), any());
        verify(sink, never()).write(any(), any(), any());
        verify(sink, never()).writeBoth(any(), any(), any());
    }

    @Test
    void shouldNotWriteIfPredicateTestsFalse() throws IOException {
        when(predicate.test(any())).thenReturn(false);

        unit.process(request).write().process(response).write();

        verify(sink, never()).write(any(), any());
        verify(sink, never()).write(any(), any(), any());
        verify(sink, never()).writeBoth(any(), any(), any());
    }

    @Test
    void shouldNeverRetrieveBodyIfInactiveWriter() throws IOException {
        when(sink.isActive()).thenReturn(false);

        unit.process(request).write();

        verify(request, never()).withBody();
    }

    @Test
    void shouldFilterRequest() throws IOException {
        unit.process(request).write();

        final ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(sink).write(any(), captor.capture());
        final HttpRequest request = captor.getValue();

        assertThat(request, instanceOf(FilteredHttpRequest.class));
    }

    @Test
    void shouldFilterResponse() throws IOException {
        unit.process(request).write().process(response).write();

        final ArgumentCaptor<HttpResponse> captor = ArgumentCaptor.forClass(HttpResponse.class);
        verify(sink).write(any(), any(), captor.capture());
        final HttpResponse response = captor.getValue();

        assertThat(response, instanceOf(FilteredHttpResponse.class));
    }

}
