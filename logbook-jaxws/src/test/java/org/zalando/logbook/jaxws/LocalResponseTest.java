package org.zalando.logbook.jaxws;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.logbook.HttpHeaders;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
public class LocalResponseTest {

    @Mock
    private SOAPMessageContext context;

    @Test
    void given_soap_11_message_should_return_information_of_response()
            throws UnsupportedEncodingException, IOException, SOAPException {
        // -- mocks
        when(context.getMessage()).thenReturn(SOAPMessageFactory.createSoap1_1());
        lenient().when(context.get(SOAPMessage.CHARACTER_SET_ENCODING)).thenReturn("UTF-8");
        lenient().when(context.get(MessageContext.HTTP_RESPONSE_CODE)).thenReturn(200);

        // -- underTest
        LocalResponse response = new LocalResponse(context);
        HttpHeaders headers = response.getHeaders();
        String contentType = response.getContentType();
        Charset charset = response.getCharset();
        int status = response.getStatus();

        // -- asserts
        assertThat(contentType, is("text/xml; charset=utf-8"));
        assertThat(charset, is(Charset.forName("UTF-8")));
        assertThat(headers.size(), is(2));
        assertThat(status, is(200));
    }
}
