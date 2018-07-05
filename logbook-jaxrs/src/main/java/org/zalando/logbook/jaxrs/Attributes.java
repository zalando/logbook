package org.zalando.logbook.jaxrs;

import org.zalando.logbook.Logbook;

final class Attributes {

    static final String CORRELATOR = Logbook.class.getName() + ".CORRELATOR";
    static final String REQUEST = Logbook.class.getName() + ".REQUEST";
    static final String RESPONSE = Logbook.class.getName() + ".RESPONSE";

    private Attributes() {

    }

}
