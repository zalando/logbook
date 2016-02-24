package org.zalando.logbook.undertow;

/*
 * #%L
 * Logbook: Undertow
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

import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.RawHttpRequest;

import io.undertow.server.HttpServerExchange;

import io.undertow.util.HeaderMap;

public final class UndertowHttpRequest extends UndertowHttpMessage implements HttpRequest, RawHttpRequest {

    public UndertowHttpRequest(final HttpServerExchange exchange) {
        super(exchange);
    }

    @Override
    public String getRemote() {
        return exchange.getSourceAddress().getAddress().getHostAddress();
    }

    @Override
    public String getMethod() {
        return exchange.getRequestMethod().toString();
    }

    @Override
    public String getRequestUri() {
        final String queryString = exchange.getQueryString();
        final String uri = exchange.getRequestURI();
        return queryString.isEmpty() ? uri : uri + '?' + queryString;
    }

    @Override
    public HttpRequest withBody() {
        return this;
    }

    @Override
    protected HeaderMap getExchangeHeaders() {
        return exchange.getRequestHeaders();
    }
}
