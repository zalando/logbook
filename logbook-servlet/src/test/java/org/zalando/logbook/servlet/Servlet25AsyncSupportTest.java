package org.zalando.logbook.servlet;

import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class Servlet25AsyncSupportTest {

    private final AsyncSupport unit = new Servlet25AsyncSupport();
    private final HttpServletRequest request = mock(HttpServletRequest.class);

    @Test
    void isFirstRequest() {
        assertTrue(unit.isFirstRequest(request));
    }

    @Test
    void isLastRequest() {
        assertTrue(unit.isLastRequest(request));
    }

}
