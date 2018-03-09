package org.zalando.logbook;

import java.util.Locale;

final class Origins {

    Origins() {
        // package private so we can trick code coverage
    }

    static String translate(final Origin origin) {
        return origin.name().toLowerCase(Locale.ROOT);
    }

}
