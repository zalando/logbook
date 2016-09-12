package org.zalando.logbook;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class BodyFilters {

    BodyFilters() {
        // package private so we can trick code coverage
    }

    public static BodyFilter defaultValue() {
        return accessToken();
    }

    public static BodyFilter accessToken() {
        final Predicate<String> json = MediaTypeQuery.compile("application/json", "application/*+json");
        final Pattern pattern = Pattern.compile("(\"(?:access_token|open_id|id_token)\"\\s*\\:\\s*)\".+?\"");

        return (contentType, body) -> json.test(contentType) ?
                pattern.matcher(body).replaceAll("$1\"XXX\"") : body;
    }

}
