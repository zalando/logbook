package org.zalando.logbook.jaxrs;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.Logbook;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

final class LogbookServerFilterTest {

    private final ContainerRequestContext request = mock(ContainerRequestContext.class);
    private final ContainerResponseContext response = mock(ContainerResponseContext.class);
    private final Logbook logbook = mock(Logbook.class);

    private final ContainerResponseFilter unit = new LogbookServerFilter(logbook);

    @Test
    void filterShouldDoNothingIfCorrelatorIsNotPresent() throws IOException {
        unit.filter(request, response);
        verifyNoInteractions(logbook);
    }

}
