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

import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public final class DefaultHttpLogFormatter implements HttpLogFormatter {

    @Override
    public String format(final Precorrelation<HttpRequest> precorrelation) throws IOException {
        return format(precorrelation.getRequest(), this::formatRequestLine);
    }

    private String formatRequestLine(final HttpRequest request) {
        return String.format("%s %s HTTP/1.1", request.getMethod(), request.getRequestUri());
    }

    @Override
    public String format(final Correlation<HttpRequest, HttpResponse> correlation) throws IOException {
        return format(correlation.getResponse(), this::formatStatusLine);
    }

    private String formatStatusLine(final HttpResponse response) {
        // TODO we are missing the reason phrase here, e.g. "OK", but there is no complete list in the JDK alone
        return String.format("HTTP/1.1 %d", response.getStatus());
    }
    
    private <H extends HttpMessage> String format(final H message, final Function<H, String> lineCreator) 
            throws IOException {
        final List<String> lines = Lists.newArrayListWithExpectedSize(4);

        lines.add(lineCreator.apply(message));
        lines.addAll(formatHeaders(message));

        final String body = message.getBodyAsString();
        
        if (!body.isEmpty()) {
            lines.add("");
            lines.add(body);
        }
        
        return join(lines);
    } 

    private List<String> formatHeaders(final HttpMessage message) {
        return message.getHeaders().asMap().entrySet().stream()
                .collect(toMap(Map.Entry::getKey, this::formatHeaderValues))
                .entrySet().stream()
                .map(this::formatHeader)
                .collect(toList());
    }

    private String formatHeaderValues(final Map.Entry<String, Collection<String>> entry) {
        return entry.getValue().stream().collect(joining(", "));
    }

    private String formatHeader(final Map.Entry<String, String> entry) {
        return String.format("%s: %s", entry.getKey(), entry.getValue());
    }

    private String join(final Collection<String> lines) {
        return lines.stream().collect(joining("\n"));
    }

}
