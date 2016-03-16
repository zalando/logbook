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

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

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

public final class DefaultLogbookTest {

    private final HttpLogFormatter formatter = mock(HttpLogFormatter.class);
    private final HttpLogWriter writer = mock(HttpLogWriter.class);
    @SuppressWarnings("unchecked") private final Predicate<RawHttpRequest> predicate = mock(Predicate.class);
    private final Obfuscator headerObfuscator = mock(Obfuscator.class);
    private final Obfuscator parameterObfuscator = mock(Obfuscator.class);
    private final BodyObfuscator bodyObfuscator = mock(BodyObfuscator.class);

    private final Logbook unit = Logbook.builder()
            .writer(writer)
            .formatter(formatter)
            .predicate(predicate)
            .headerObfuscator(headerObfuscator)
            .parameterObfuscator(parameterObfuscator)
            .bodyObfuscator(bodyObfuscator)
            .build();

    private final RawHttpRequest rawHttpRequest = mock(RawHttpRequest.class);
    private final RawHttpResponse rawHttpResponse = mock(RawHttpResponse.class);

    private final HttpRequest request = mock(HttpRequest.class);
    private final HttpResponse response = mock(HttpResponse.class);

    @Before
    public void defaultBehaviour() throws IOException {
        when(writer.isActive(any())).thenReturn(true);
        when(predicate.test(any())).thenReturn(true);
        when(rawHttpRequest.withBody()).thenReturn(request);
        when(rawHttpResponse.withBody()).thenReturn(response);
    }

    @Test
    public void shouldNotReturnCorrelatorIfInactiveWriter() throws IOException {
        when(writer.isActive(any())).thenReturn(false);

        final Optional<Correlator> correlator = unit.write(rawHttpRequest);

        assertThat(correlator, hasFeature("present", Optional::isPresent, is(false)));
    }
    
    @Test
    public void shouldNotReturnCorrelatorIfPredicateTestsFalse() throws IOException {
        when(writer.isActive(any())).thenReturn(true);
        when(predicate.test(any())).thenReturn(false);

        final Optional<Correlator> correlator = unit.write(rawHttpRequest);

        assertThat(correlator, hasFeature("present", Optional::isPresent, is(false)));
    }

    @Test
    public void shouldNeverRetrieveBodyIfInactiveWriter() throws IOException {
        when(writer.isActive(any())).thenReturn(false);

        unit.write(rawHttpRequest);

        verify(rawHttpRequest, never()).withBody();
    }

    @Test
    public void shouldObfuscateRequest() throws IOException {
        final Correlator correlator = unit.write(rawHttpRequest).get();

        correlator.write(rawHttpResponse);

        @SuppressWarnings("unchecked")
        final ArgumentCaptor<Precorrelation<HttpRequest>> captor = ArgumentCaptor.forClass(Precorrelation.class);
        verify(formatter).format(captor.capture());
        final Precorrelation<HttpRequest> precorrelation = captor.getValue();

        assertThat(precorrelation.getRequest(), instanceOf(ObfuscatedHttpRequest.class));
    }

    @Test
    public void shouldObfuscateResponse() throws IOException {
        final Correlator correlator = unit.write(rawHttpRequest).get();

        correlator.write(rawHttpResponse);

        @SuppressWarnings("unchecked")
        final ArgumentCaptor<Correlation<HttpRequest, HttpResponse>> captor = ArgumentCaptor.forClass(Correlation.class);
        verify(formatter).format(captor.capture());
        final Correlation<HttpRequest, HttpResponse> correlation = captor.getValue();

        assertThat(correlation.getRequest(), instanceOf(ObfuscatedHttpRequest.class));
        assertThat(correlation.getResponse(), instanceOf(ObfuscatedHttpResponse.class));
    }

}