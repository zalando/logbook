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
        final Predicate<T> contentTypes = contentType(
                "application/json-seq", // https://tools.ietf.org/html/rfc7464
                "application/x-json-stream", // https://en.wikipedia.org/wiki/JSON_Streaming#Line_delimited_JSON
                "application/stream+json", // https://tools.ietf.org/html/draft-snell-activity-streams-type-01
                "text/event-stream" // https://tools.ietf.org/html/rfc6202
        );
        return replaceBody(contentTypes, "<stream>");
    }

    public static <T extends BaseHttpMessage> BodyReplacer<T> replaceBody(final Predicate<T> predicate,
            final String replacement) {
        return message -> predicate.test(message) ? replacement : null;
    }

}
