package org.zalando.logbook;

import static org.zalando.logbook.Conditions.contentType;

public final class Replacers {

    Replacers() {
        // package private so we can trick code coverage
    }

    public static <T extends BaseHttpMessage> Replacer<T> defaultValue() {
        return Replacer.compound(binary(), stream());
    }

    public static <T extends BaseHttpMessage> Replacer<T> binary() {
        // TODO more content types
        return Replacer.replaceWith(contentType("image/*"), "<binary>");
    }

    public static <T extends BaseHttpMessage> Replacer<T> stream() {
        // TODO more content types
        return Replacer.replaceWith(contentType("application/json-seq"), "<stream>");
    }

}
