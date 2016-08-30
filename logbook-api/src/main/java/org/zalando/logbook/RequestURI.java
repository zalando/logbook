package org.zalando.logbook;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.zalando.logbook.RequestURI.Component.AUTHORITY;
import static org.zalando.logbook.RequestURI.Component.PATH;
import static org.zalando.logbook.RequestURI.Component.QUERY;
import static org.zalando.logbook.RequestURI.Component.SCHEME;

final class RequestURI {

    RequestURI() {
        // package private so we can trick code coverage
    }

    enum Component {
        SCHEME, AUTHORITY, PATH, QUERY
    }

    static String reconstruct(final BaseHttpRequest request) {
        return reconstruct(request, EnumSet.allOf(Component.class));
    }

    static String reconstruct(final BaseHttpRequest request, final Component... components) {
        return reconstruct(request, EnumSet.copyOf(asList(components)));
    }

    private static String reconstruct(final BaseHttpRequest request, final Set<Component> components) {
        final String scheme = request.getScheme();
        final String host = request.getHost();
        final Optional<Integer> port = request.getPort();
        final String path = request.getPath();
        final String query = request.getQuery();

        final StringBuilder url = new StringBuilder();

        if (components.contains(SCHEME)) {
            url.append(scheme).append(":");
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

        return url.toString();
    }

    private static boolean isNotStandardPort(final String scheme, final int port) {
        return "http".equals(scheme) && port != 80 ||
                "https".equals(scheme) && port != 443;
    }

}
