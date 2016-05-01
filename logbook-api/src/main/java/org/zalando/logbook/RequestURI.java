package org.zalando.logbook;

/*
 * #%L
 * Logbook: API
 * %%
 * Copyright (C) 2015 - 2016 Zalando SE
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.EnumSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.EnumSet.of;
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
        final int port = request.getPort();
        final String path = request.getPath();
        final String query = request.getQuery();

        final StringBuilder url = new StringBuilder();

        if (components.contains(SCHEME)) {
            url.append(scheme).append(":");
        }

        if (components.contains(AUTHORITY)) {
            url.append("//").append(host);

            if (isNotStandardPort(scheme, port)) {
                url.append(':').append(port);
            }
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
