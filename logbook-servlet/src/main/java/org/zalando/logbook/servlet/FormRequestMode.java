package org.zalando.logbook.servlet;

import javax.annotation.Nullable;

import static java.util.Locale.ROOT;

enum FormRequestMode {

    BODY, PARAMETER, OFF;

    static FormRequestMode fromProperties() {
        @Nullable final String property = System.getProperty("logbook.servlet.form-request");

        if (property == null) {
            return BODY;
        }

        return FormRequestMode.valueOf(property.toUpperCase(ROOT));
    }

}
