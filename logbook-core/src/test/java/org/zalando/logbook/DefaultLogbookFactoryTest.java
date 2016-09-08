package org.zalando.logbook;

import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DefaultLogbookFactoryTest {

    @Test
    public void shouldDefaultToAlwaysTruePredicate() throws IOException {
        final HttpLogWriter writer = mock(HttpLogWriter.class);
        when(writer.isActive(any())).thenReturn(true);

        final Logbook logbook = Logbook.builder()
                .writer(writer)
                .build();

        final Optional<Correlator> correlator = logbook.write(MockRawHttpRequest.create());

        assertThat(correlator, is(not(empty())));
    }

}