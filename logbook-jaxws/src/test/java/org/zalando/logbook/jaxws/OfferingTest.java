package org.zalando.logbook.jaxws;

import org.junit.jupiter.api.Test;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class OfferingTest {

    @Test
    void anySOAPExceptionIsTranslatedToIOException() throws SOAPException, IOException {
        final Offering unit = new Offering();
        final SOAPMessage message = mock(SOAPMessage.class);
        doThrow(new SOAPException()).when(message).writeTo(any());

        final IOException exception = assertThrows(IOException.class, () -> unit.buffer(message));

        assertThat(exception)
                .getCause().isInstanceOf(SOAPException.class);
    }

    @Test
    void defaultsToEmptyBody() {
        assertThat(new Offering().getBody()).isEmpty();
    }

}