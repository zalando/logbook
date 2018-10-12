package org.zalando.logbook;

import org.apiguardian.api.API;

import java.util.regex.Pattern;

import static java.util.regex.Pattern.quote;
import static org.apiguardian.api.API.Status.MAINTAINED;
import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public final class QueryFilters {

    private QueryFilters() {
    }

    @API(status = MAINTAINED)
    public static QueryFilter defaultValue() {
        return accessToken();
    }

    @API(status = MAINTAINED)
    public static QueryFilter accessToken() {
        return replaceQuery("access_token", "XXX");
    }

    @API(status = MAINTAINED)
    public static QueryFilter replaceQuery(final String name, final String replacement) {
        final Pattern pattern = Pattern.compile("((?:^|&)" + quote(name) + "=)(?:.*?)(&|$)");
        final String replacementPattern = "$1" + replacement + "$2";

        return query -> pattern.matcher(query).replaceAll(replacementPattern);
    }

    @API(status = MAINTAINED)
    public static QueryFilter removeQuery(final String name) {
        final Pattern pattern = Pattern.compile("((?:^|&)" + quote(name) + "=)(?:.*?)(&|$)");
        return query -> pattern.matcher(query).replaceAll("$2").replaceFirst("^&", "");
    }

}
