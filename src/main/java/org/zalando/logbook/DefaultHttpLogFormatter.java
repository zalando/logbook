package org.zalando.logbook;

/*
 * #%L
 * Logbook
 * %%
 * Copyright (C) 2015 Zalando SE
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

import com.google.common.collect.Multimap;

import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.zalando.logbook.Formatting.getHeaders;

public final class DefaultHttpLogFormatter implements HttpLogFormatter {

    @Override
    public String format(final TeeHttpServletRequest request) throws IOException {
        final List<String> lines = new ArrayList<>();

        lines.add(formatRequestLine(request));
        lines.addAll(formatHeaders(getHeaders(request)));

        final String body = request.getBodyAsString();
        if (!body.isEmpty()) {
            lines.add("");
            lines.add(body);
        }

        return join(lines);
    }

    private String formatRequestLine(final TeeHttpServletRequest request) {
        return String.format("%s %s HTTP/1.1", request.getMethod(), request.getRequestURI());
    }

    @Override
    public String format(final TeeHttpServletResponse response) throws IOException {
        final List<String> lines = new ArrayList<>();

        lines.add(formatStatusLine(response));
        lines.addAll(formatHeaders(getHeaders(response)));

        final String body = response.getBodyAsString();
        if (!body.isEmpty()) {
            lines.add("");
            lines.add(body);
        }

        return join(lines);
    }

    private String formatStatusLine(final TeeHttpServletResponse response) {
        // TODO we are missing the reason phrase here, e.g. "OK", but there is no complete list in the JDK alone
        return String.format("HTTP/1.1 %d", response.getStatus());
    }

    static List<String> formatHeaders(final Multimap<String, String> headers) {
        return headers.asMap().entrySet().stream()
                .collect(toMap(Map.Entry::getKey, DefaultHttpLogFormatter::formatHeaderValues))
                .entrySet().stream()
                .map(DefaultHttpLogFormatter::formatHeader)
                .collect(toList());
    }

    static String formatHeaderValues(final Map.Entry<String, Collection<String>> entry) {
        return entry.getValue().stream().collect(joining(", "));
    }

    static String formatHeader(final Map.Entry<String, String> entry) {
        return String.format("%s: %s", entry.getKey(), entry.getValue());
    }

    private String join(final Collection<String> lines) {
        return lines.stream().collect(joining("\n"));
    }

}
