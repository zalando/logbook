package org.zalando.logbook.servlet;

import net.jcip.annotations.NotThreadSafe;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.zalando.logbook.servlet.junit.RestoreSystemProperties;


import static java.util.Locale.ROOT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.zalando.logbook.servlet.FormRequestMode.BODY;

@NotThreadSafe
@RestoreSystemProperties
class FormRequestModeTest {

    @ParameterizedTest
    @CsvSource({"off", "body", "parameter"})
    void test(final String value) {
        System.setProperty("logbook.servlet.form-request", value);
        final FormRequestMode unit = FormRequestMode.fromProperties();
        assertEquals(value, unit.name().toLowerCase(ROOT));
    }

    @Test
    void defaultsToBody() {
        assertEquals(BODY, FormRequestMode.fromProperties());
    }

}
