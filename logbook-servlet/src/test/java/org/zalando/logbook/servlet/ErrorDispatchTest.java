package org.zalando.logbook.servlet;

import com.google.common.collect.ImmutableMultimap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.HttpMessage;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;

import javax.servlet.DispatcherType;
import java.io.IOException;

import static com.jayway.jsonassert.JsonAssert.with;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Verifies that {@link LogbookFilter} handles {@link DispatcherType#ERROR} correctly.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
@WebAppConfiguration
public final class ErrorDispatchTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private HttpLogFormatter formatter;

    @Autowired
    private HttpLogWriter writer;

    @Before
    public void setUp() throws IOException {
        reset(formatter, writer);

        when(writer.isActive(any())).thenReturn(true);
    }

    @Test
    public void shouldFormatErrorResponse() throws Exception {
        mvc.perform(get("/api/not-found"));

        final HttpResponse response = interceptResponse();

        assertThat(response, hasFeature("status", HttpResponse::getStatus, is(404)));
        assertThat(response, hasFeature("headers", HttpResponse::getHeaders, is(ImmutableMultimap.of())));
    }

    private String getBodyAsString(final HttpMessage message) {
        try {
            return message.getBodyAsString();
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

    private HttpRequest interceptRequest() throws IOException {
        final ArgumentCaptor<Precorrelation> captor = ArgumentCaptor.forClass(Precorrelation.class);
        verify(formatter).format(captor.capture());
        return captor.getValue().getRequest();
    }

    private HttpResponse interceptResponse() throws IOException {
        final ArgumentCaptor<Correlation> captor = ArgumentCaptor.forClass(Correlation.class);
        verify(formatter).format(captor.capture());
        return captor.getValue().getResponse();
    }

}
