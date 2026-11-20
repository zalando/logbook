package org.zalando.logbook.servlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.zalando.logbook.Logbook;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SecureLogbookFilterTest {

    @Test
    void shouldCreateFilterWithDefaults() throws ServletException, IOException {
        final SecureLogbookFilter filter = new SecureLogbookFilter();
        assertNotNull(filter);

        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test");
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final FilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);
    }

    @Test
    void shouldSupportWithMethods() {
        final SecureLogbookFilter filter = new SecureLogbookFilter(Logbook.create())
                .withAsyncOnCompleteListenerWrapper(AsyncOnCompleteListenerWrapper.identity())
                .withMaxBufferSize(1024L)
                .withFormRequestMode(FormRequestMode.BODY);

        assertNotNull(filter);
    }

}
