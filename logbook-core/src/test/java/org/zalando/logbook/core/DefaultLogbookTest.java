package org.zalando.logbook.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;
import org.zalando.logbook.BodyFilter;
import org.zalando.logbook.HeaderFilter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.QueryFilter;
import org.zalando.logbook.Sink;
import org.zalando.logbook.attributes.AttributeExtractor;
import org.zalando.logbook.test.MockHttpRequest;
import org.zalando.logbook.test.MockHttpResponse;

import java.io.IOException;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

final class DefaultLogbookTest {

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

    private static Answer<?> delegateTo(final Object delegate) {
        return invocation ->
                invocation.getMethod().invoke(delegate, invocation.getArguments());
    }

    @BeforeEach
    void defaultBehaviour() {
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

        assertThat(request).isInstanceOf(FilteredHttpRequest.class);
    }

    @Test
    void shouldFilterResponse() throws IOException {
        unit.process(request).write().process(response).write();

        final ArgumentCaptor<HttpResponse> captor = ArgumentCaptor.forClass(HttpResponse.class);
        verify(sink).write(any(), any(), captor.capture());
        final HttpResponse response = captor.getValue();

        assertThat(response).isInstanceOf(FilteredHttpResponse.class);
    }

    @Test
    void shouldNotThrowEvenIfAttributeExtractorThrows() throws Exception {
        final AttributeExtractor exceptionThrowingAttributeExtractor = mock(AttributeExtractor.class);
        when(exceptionThrowingAttributeExtractor.extract(any())).thenThrow(new Exception());
        when(exceptionThrowingAttributeExtractor.extract(any(), any())).thenThrow(new Exception());

        final Logbook logbook = Logbook.builder()
                .attributeExtractor(exceptionThrowingAttributeExtractor)
                .sink(sink)
                .build();

        assertThatCode(() -> logbook.process(request)).doesNotThrowAnyException();
    }

}
