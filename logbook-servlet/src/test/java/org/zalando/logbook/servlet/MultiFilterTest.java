package org.zalando.logbook.servlet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Precorrelation;

import javax.servlet.ServletRequest;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Verifies that {@link LogbookFilter} handles cases correctly when multiple instances are running in the same chain.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MultiFilterTestConfiguration.class)
@WebAppConfiguration
public final class MultiFilterTest {

    private final String uri = "/api/sync";

    @Autowired
    private MockMvc mvc;

    @Autowired
    @First
    private HttpLogFormatter firstFormatter;

    @Autowired
    @First
    private HttpLogWriter firstWriter;

    @Autowired
    @Second
    private HttpLogFormatter secondFormatter;

    @Autowired
    @Second
    private HttpLogWriter secondWriter;

    @Before
    public void setUp() throws IOException {
        reset(firstFormatter, secondFormatter, firstWriter, secondWriter);

        when(firstWriter.isActive(any())).thenReturn(true);
        when(secondWriter.isActive(any())).thenReturn(true);
    }

    @Test
    public void shouldFormatRequestTwice() throws Exception {
        mvc.perform(get(uri));

        verify(firstFormatter).format(any(Precorrelation.class));
        verify(secondFormatter).format(any(Precorrelation.class));
    }

    @Test
    public void shouldFormatResponseTwice() throws Exception {
        mvc.perform(get(uri));

        verify(firstFormatter).format(any(Correlation.class));
        verify(secondFormatter).format(any(Correlation.class));
    }

    @Test
    public void shouldLogRequestTwice() throws Exception {
        mvc.perform(get(uri));

        verify(firstWriter).writeRequest(any());
        verify(secondWriter).writeRequest(any());
    }

    @Test
    public void shouldLogResponseTwice() throws Exception {
        mvc.perform(get(uri));

        verify(firstWriter).writeResponse(any());
        verify(secondWriter).writeResponse(any());
    }

    @Test
    public void shouldBufferRequestOnlyOnce() throws Exception {
        final MvcResult result = mvc.perform(get(uri)).andReturn();

        final MockHttpServletRequest request = result.getRequest();

        final List<TeeRequest> teeRequests = getList(request, TestAttributes.REQUESTS);

        assertThat(teeRequests, is(notNullValue()));
        assertThat(teeRequests, hasSize(2));

        // TODO verify buffer
    }

    @Test
    public void shouldBufferResponseOnlyOnce() throws Exception {
        final MvcResult result = mvc.perform(get(uri)).andReturn();

        final MockHttpServletRequest request = result.getRequest();

        final List<TeeResponse> teeResponses = getList(request, TestAttributes.RESPONSES);
        assertThat(teeResponses, hasSize(2));

        final TeeResponse firstResponse = teeResponses.get(0);
        final TeeResponse secondResponse = teeResponses.get(1);

        assertThat(firstResponse.getOutput().toByteArray().length, is(equalTo(0)));
        assertThat(secondResponse.getOutput().toByteArray().length, is(greaterThan(0)));
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> getList(final ServletRequest request, final String attributeName) {
        return (List<T>) request.getAttribute(attributeName);
    }

}
