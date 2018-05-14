package org.zalando.logbook;

import java.util.Locale;

final class Origins {

    private Origins() {

    }

    static String translate(final Origin origin) {
        return origin.name().toLowerCase(Locale.ROOT);
    }

}
