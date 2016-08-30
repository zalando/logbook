package org.zalando.logbook.servlet;

import org.zalando.logbook.Logbook;

final class Attributes {

    static final String CORRELATOR = Logbook.class.getName() + ".CORRELATOR";

    Attributes() {
        // package private so we can trick code coverage
    }

}
