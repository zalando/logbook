package org.zalando.logbook.servlet;

import org.zalando.logbook.Logbook;

final class Attributes {

    static final String CORRELATION = Logbook.class.getName() + ".CORRELATION";

    static final String REQUEST_BODY = Logbook.class.getName() + ".REQUEST_BODY";

    static final String RESPONSE_BODY = Logbook.class.getName() + ".RESPONSE_BODY";

    /**
     * Will be set to a non-null value as soon as the latest {@link LogbookFilter} starts buffering the response body.
     */
    static final String BUFFERING = Logbook.class.getName() + ".BUFFERING";

    Attributes() {
        // package private so we can trick code coverage
    }

}
