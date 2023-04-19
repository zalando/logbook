package org.zalando.logbook.jaxrs;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.Logbook;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

final class LogbookClientFilterTest {

    private final ClientRequestContext request = mock(ClientRequestContext.class);
    private final ClientResponseContext response = mock(ClientResponseContext.class);
    private final Logbook logbook = mock(Logbook.class);

    private final LogbookClientFilter unit = new LogbookClientFilter(logbook);

    @Test
    void filterShouldDoNothingIfStageIsNotPresent() {
        unit.filter(request, response);
        verifyNoInteractions(logbook);
    }

}
