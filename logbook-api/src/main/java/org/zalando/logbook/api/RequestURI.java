package org.zalando.logbook.api;

import lombok.experimental.UtilityClass;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.zalando.logbook.api.RequestURI.Component.AUTHORITY;
import static org.zalando.logbook.api.RequestURI.Component.PATH;
import static org.zalando.logbook.api.RequestURI.Component.QUERY;
import static org.zalando.logbook.api.RequestURI.Component.SCHEME;

@UtilityClass
public final class RequestURI {

    public enum Component {
        SCHEME, AUTHORITY, PATH, QUERY
    }

    public static String reconstruct(final HttpRequest request) {
        final StringBuilder url = new StringBuilder();
        reconstruct(request, url);
        return url.toString();
    }

    public static void reconstruct(final HttpRequest request, final StringBuilder output) {
        reconstruct(request, EnumSet.allOf(Component.class), output);
    }

    public static String reconstruct(final HttpRequest request, final Component... components) {
        final StringBuilder url = new StringBuilder();
        reconstruct(request, EnumSet.copyOf(asList(components)), url);
        return url.toString();
    }

    public static String reconstruct(final HttpRequest request, final Set<Component> components) {
        final StringBuilder url = new StringBuilder();
        reconstruct(request, components, url);
        return url.toString();
    }

    private static void reconstruct(final HttpRequest request, final Set<Component> components, final StringBuilder url) {
        final String scheme = request.getScheme();
        final String host = request.getHost();
        final Optional<Integer> port = request.getPort();
        final String path = request.getPath();
        final String query = request.getQuery();

        if (components.contains(SCHEME)) {
            url.append(scheme).append(':');
        }

        if (components.contains(AUTHORITY)) {
            url.append("//").append(host);

            port.ifPresent(p -> {
                if (isNotStandardPort(scheme, p)) {
                    url.append(':').append(p);
                }
            });

        } else if (components.contains(SCHEME)) {
            url.append("//");
        }

        if (components.contains(PATH)) {
            url.append(path);
        } else {
            url.append('/');
        }

        if (components.contains(QUERY) && !query.isEmpty()) {
            url.append('?').append(query);
        }
    }

    private static boolean isNotStandardPort(final String scheme, final int port) {
        return ("http".equals(scheme) && port != 80) ||
                ("https".equals(scheme) && port != 443);
    }

}
