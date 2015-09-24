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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class JsonHttpLogger implements HttpLogger {

    private static final Logger LOG = LoggerFactory.getLogger(JsonHttpLogger.class);

    private final ObjectMapper mapper;

    public JsonHttpLogger() {
        this(new ObjectMapper()
                .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES));
    }

    public JsonHttpLogger(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean shouldLog(final HttpServletRequest request, final HttpServletResponse response) {
        return LOG.isTraceEnabled();
    }

    @Override
    public void logRequest(RequestData request) {
        try {
            LOG.trace("Incoming: [{}]", mapper.writeValueAsString(request));
        } catch (JsonProcessingException e) {
            LOG.warn("Error trying to log request", e);
        }
    }

    @Override
    public void logResponse(ResponseData response) {
        try {
            LOG.trace("Outgoing: [{}]", mapper.writeValueAsString(response));
        } catch (JsonProcessingException e) {
            LOG.warn("Error trying to log response", e);
        }
    }

}
