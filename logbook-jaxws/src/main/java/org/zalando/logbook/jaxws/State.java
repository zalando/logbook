package org.zalando.logbook.jaxws;

import javax.xml.soap.SOAPMessage;
import java.io.IOException;

interface State {

    default State with() {
        return this;
    }

    default State without() {
        return this;
    }

    default State buffer(final SOAPMessage context) throws IOException {
        return this;
    }

    default byte[] getBody() {
        return new byte[0];
    }

}
