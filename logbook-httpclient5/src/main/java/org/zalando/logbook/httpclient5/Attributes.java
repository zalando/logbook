package org.zalando.logbook.httpclient5;

import lombok.experimental.UtilityClass;
import org.zalando.logbook.Logbook;

@UtilityClass
final class Attributes {
    static final String STAGE = Logbook.class.getName() + ".STAGE";
}
