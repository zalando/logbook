package org.zalando.logbook.jaxrs;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.Logbook;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

public final class LogbookServerFilterTest {

    private final ContainerRequestContext request = mock(ContainerRequestContext.class);
    private final ContainerResponseContext response = mock(ContainerResponseContext.class);
    private final Logbook logbook = mock(Logbook.class);

    private final ContainerResponseFilter unit = new LogbookServerFilter(logbook);

    @Test
    public void filterShouldDoNothingIfCorrelatorIsNotPresent() throws IOException {
        unit.filter(request, response);
        verifyZeroInteractions(logbook);
    }

}
