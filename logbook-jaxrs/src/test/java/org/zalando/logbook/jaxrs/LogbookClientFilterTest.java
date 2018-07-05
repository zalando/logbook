package org.zalando.logbook.jaxrs;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.Logbook;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

public final class LogbookClientFilterTest {

    private final ClientRequestContext request = mock(ClientRequestContext.class);
    private final ClientResponseContext response = mock(ClientResponseContext.class);
    private final Logbook logbook = mock(Logbook.class);

    private final LogbookClientFilter unit = new LogbookClientFilter(logbook);

    @Test
    public void filterShouldDoNothingIfCorrelatorIsNotPresent() {
        unit.filter(request, response);
        verifyZeroInteractions(logbook);
    }

}
