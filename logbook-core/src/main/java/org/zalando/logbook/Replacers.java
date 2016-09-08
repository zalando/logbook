package org.zalando.logbook;

import java.util.function.Predicate;

import static org.zalando.logbook.Conditions.contentType;
import static org.zalando.logbook.Replacer.replaceWith;

public final class Replacers {

    Replacers() {
        // package private so we can trick code coverage
    }

    public static <T extends BaseHttpMessage> Replacer<T> defaultValue() {
        return Replacer.compound(binary(), multipart(), stream());
    }

    public static <T extends BaseHttpMessage> Replacer<T> binary() {
        final Predicate<T> contentTypes = contentType(
                "application/octet-stream", "application/pdf", "audio/*", "image/*", "video/*");
        return replaceWith(contentTypes, "<binary>");
    }

    public static <T extends BaseHttpMessage> Replacer<T> multipart() {
        return replaceWith(contentType("multipart/*"), "<multipart>");
    }

    public static <T extends BaseHttpMessage> Replacer<T> stream() {
        final Predicate<T> contentTypes = contentType("application/json-seq", "application/x-json-stream");
        return replaceWith(contentTypes, "<stream>");
    }

}
