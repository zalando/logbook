package org.zalando.logbook.jaxws;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

final class Offering implements State {

    @Override
    public State without() {
        return new Unbuffered();
    }

    @Override
    public State buffer(final SOAPMessage message) throws IOException {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
            message.writeTo(stream);
        } catch (final SOAPException e) {
            throw new IOException(e);
        }

        return new Buffering(stream.toByteArray());
    }

}
