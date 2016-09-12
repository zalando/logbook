package org.zalando.logbook;

import java.util.function.Predicate;

import static org.zalando.logbook.Conditions.contentType;

public final class BodyReplacers {

    BodyReplacers() {
        // package private so we can trick code coverage
    }

    public static <T extends BaseHttpMessage> BodyReplacer<T> defaultValue() {
        return BodyReplacer.compound(binary(), multipart(), stream());
    }

    public static <T extends BaseHttpMessage> BodyReplacer<T> binary() {
        final Predicate<T> contentTypes = contentType(
                "application/octet-stream", "application/pdf", "audio/*", "image/*", "video/*");
        return replaceBody(contentTypes, "<binary>");
    }

    public static <T extends BaseHttpMessage> BodyReplacer<T> multipart() {
        return replaceBody(contentType("multipart/*"), "<multipart>");
    }

    public static <T extends BaseHttpMessage> BodyReplacer<T> stream() {
        final Predicate<T> contentTypes = contentType("application/json-seq", "application/x-json-stream");
        return replaceBody(contentTypes, "<stream>");
    }

    public static <T extends BaseHttpMessage> BodyReplacer<T> replaceBody(final Predicate<T> predicate,
            final String replacement) {
        return message -> predicate.test(message) ? replacement : null;
    }

}
