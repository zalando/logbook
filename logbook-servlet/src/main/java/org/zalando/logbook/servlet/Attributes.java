package org.zalando.logbook.servlet;

import org.zalando.logbook.Logbook;

final class Attributes {

    static final String CORRELATION = Logbook.class.getName() + ".CORRELATION_ID";
    static final String REQUEST_BODY = Logbook.class.getName() + ".REQUEST_BODY";
    static final String RESPONSE_BODY = Logbook.class.getName() + ".RESPONSE_BODY";

    Attributes() {
        // package private so we can trick code coverage
    }

}
