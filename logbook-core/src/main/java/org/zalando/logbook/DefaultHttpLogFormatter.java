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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.Multimap;
import com.google.gag.annotation.remark.Hack;
import com.google.gag.annotation.remark.OhNoYouDidnt;
import lombok.SneakyThrows;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Multimaps.transformValues;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public final class DefaultHttpLogFormatter implements HttpLogFormatter {

    @Override
    public String format(final Precorrelation precorrelation) throws IOException {
        final HttpRequest request = precorrelation.getRequest();
        final List<String> lines = new ArrayList<>();

        lines.add(formatRequestLine(request));
        lines.addAll(formatHeaders(request.getHeaders()));

        final String body = request.getBodyAsString();
        if (!body.isEmpty()) {
            lines.add("");
            lines.add(body);
        }

        return join(lines);
    }

    private String formatRequestLine(final HttpRequest request) {
        // TODO find a better way to preserve the original request line (URI + query string) and POST form parameters
        return String.format("%s %s HTTP/1.1", request.getMethod(), getRequestURI(request));
    }

    private String getRequestURI(final HttpRequest request) {
        final String uri = request.getRequestURI();
        final Multimap<String, String> parameters = request.getParameters();

        if (parameters.isEmpty()) {
            return uri;
        }

        return uri + "?" + urlEncodeUTF8(parameters);

    }

    String urlEncodeUTF8(final Multimap<String, String> map) {
        final Joiner.MapJoiner joiner = Joiner.on("&").withKeyValueSeparator("=").useForNull("");

        // TODO encode keys as well
        final Multimap<String, String> values = transformValues(map, this::urlEncodeUTF8);

        return joiner.join(values.entries());
    }

    @VisibleForTesting
    String urlEncodeUTF8(@Nullable final String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (@Hack("Just so we can trick the code coverage") @OhNoYouDidnt final Exception e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public String format(final Correlation correlation) throws IOException {
        final HttpResponse response = correlation.getResponse();
        final List<String> lines = new ArrayList<>();

        lines.add(formatStatusLine(response));
        lines.addAll(formatHeaders(response.getHeaders()));

        final String body = response.getBodyAsString();
        if (!body.isEmpty()) {
            lines.add("");
            lines.add(body);
        }

        return join(lines);
    }

    private String formatStatusLine(final HttpResponse response) {
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
