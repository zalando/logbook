package org.zalando.springframework.web.logging;

/*
 * #%L
 * spring-web-logging
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public final class DefaultHttpLogger implements HttpLogger {

    private static final String IN = "< ";
    private static final String OUT = "> ";
    private static final String BR = "\n";
    private static final String INSET = "*   ";

    private static final Logger LOG = LoggerFactory.getLogger(DefaultHttpLogger.class);

    @Override
    public boolean shouldLog(final HttpServletRequest request, final HttpServletResponse response) {
        return LOG.isTraceEnabled();
    }

    @Override
    public void logRequest(final RequestData request) {
        final StringBuilder builder = new StringBuilder((request.getHeaders().size() * 15) + request.getBody().length() + 100);

        builder.append(IN).append("Method=").append(request.getMethod());
        builder.append(BR);

        builder.append(IN).append("URL=").append(request.getUrl());
        builder.append(BR);

        builder.append(IN).append("Client=").append(request.getRemote());
        builder.append(BR);

        builder.append(IN).append("Parameter=");
        builder.append(BR);
        logMap(builder, request.getParameters());

        builder.append(IN).append("Header=");
        builder.append(BR);
        logMap(builder, request.getHeaders());

        builder.append(IN).append("Body=").append(request.getBody());
        builder.append(BR);

        builder.append(IN).append("End.");

        LOG.trace(builder.toString());
    }

    @Override
    public void logResponse(final ResponseData response) {
        final StringBuilder builder = new StringBuilder((response.getHeaders().size() * 15) + response.getBody().length() + 100);

        builder.append(OUT).append("Status=").append(response.getStatus());
        builder.append(BR);

        builder.append(OUT).append("Header=");
        builder.append(BR);
        logMap(builder, response.getHeaders());

        builder.append(OUT).append("Content-Type=").append(response.getContentType());

        builder.append(BR);
        builder.append(OUT).append("Body=").append(response.getBody());

        builder.append(BR);
        builder.append(OUT).append("End.");

        LOG.trace(builder.toString());
    }

    private void logMap(final StringBuilder builder, final Map<String, List<String>> parameters) {
        parameters.entrySet()
                  .forEach(entry -> entry.getValue().stream()
                                         .forEach(p -> builder.append(INSET).append(entry.getKey()).append(": ").append(p).append(BR)));
    }
}
