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

final class RequestURI {

    static String reconstruct(final BaseHttpRequest request) {
        final String scheme = request.getScheme();
        final String host = request.getHost();
        final int port = request.getPort();
        final String path = request.getPath();
        final String query = request.getQuery();

        final StringBuilder url = new StringBuilder()
                .append(scheme).append("://").append(host);

        if ("http".equals(scheme) && port != 80 ||
                "https".equals(scheme) && port != 443) {
            url.append(':').append(port);
        }

        url.append(path);

        if (!query.isEmpty()) {
            url.append('?').append(query);
        }

        return url.toString();
    }

}
