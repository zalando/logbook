package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DefaultLogbookFactoryTest {

    @Test
    void shouldDefaultToAlwaysTruePredicate() throws IOException {
        final HttpLogWriter writer = mock(HttpLogWriter.class);
        when(writer.isActive(any())).thenReturn(true);

        final Logbook logbook = Logbook.builder()
                .writer(writer)
                .build();

        final Optional<Correlator> correlator = logbook.write(MockRawHttpRequest.create());

        assertThat(correlator, is(not(empty())));
    }

}
