package org.zalando.logbook.jaxws;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
public class RequestBodyTest {

    @Mock
    private SOAPMessageContext context;

    @Test
    void given_soap_11_message_should_return_empty_body()
            throws UnsupportedEncodingException, IOException, SOAPException {
        // -- mocks
        when(context.getMessage()).thenReturn(SOAPMessageFactory.createSoap1_1());

        // -- underTest
        LocalRequest request = new LocalRequest(context);
        byte[] bodyEmpty = request.getBody();

        // -- asserts
        assertArrayEquals(new byte[0], bodyEmpty);
    }

    @Test
    void given_soap_11_message_should_return_body() throws UnsupportedEncodingException, IOException, SOAPException {
        // -- mocks
        when(context.getMessage()).thenReturn(SOAPMessageFactory.createSoap1_1());

        // -- underTest
        LocalRequest request = new LocalRequest(context);
        byte[] body = request.withBody().getBody();

        // -- asserts
        assertTrue(body.length > 0);
    }

    @Test
    void given_soap_message_should_return_exceptionS() throws SOAPException, IOException {
        // -- mocks
        SOAPMessage message = Mockito.mock(SOAPMessage.class);
        when(context.getMessage()).thenReturn(message);
        doThrow(new SOAPException()).when(message).writeTo(Mockito.any());

        // -- underTest
        LocalRequest request = new LocalRequest(context);
        assertThrows(IOException.class, () -> {
            request.withBody().getBody();
        });
    }
}