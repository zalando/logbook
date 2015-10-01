package org.zalando.logbook.servlet;

/*
 * #%L
 * Logbook: Servlet
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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.io.ByteStreams;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.RawHttpRequest;

import javax.annotation.Nullable;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Optional;

import static com.google.common.collect.Iterators.addAll;
import static com.google.common.collect.Iterators.forEnumeration;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

final class TeeRequest extends HttpServletRequestWrapper implements RawHttpRequest, HttpRequest {

    /**
     * Null until we a) capture it ourselves or b) retrieve it from {@link Attributes#REQUEST_BODY}, which
     * was previously captured by another filter instance.
     */
    private byte[] body;

    TeeRequest(final HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getRemote() {
        return getRemoteAddr();
    }

    @Override
    public Multimap<String, String> getHeaders() {
        final Multimap<String, String> headers = ArrayListMultimap.create();
        final UnmodifiableIterator<String> iterator = forEnumeration(getHeaderNames());

        while (iterator.hasNext()) {
            final String header = iterator.next();
            addAll(headers.get(header), forEnumeration(getHeaders(header)));
        }

        return headers;
    }

    @Override
    public Charset getCharset() {
        return Optional.ofNullable(getCharacterEncoding()).map(Charset::forName).orElse(ISO_8859_1);
    }

    @Override
    public Multimap<String, String> getParameters() {
        final Multimap<String, String> parameters = ArrayListMultimap.create();

        getParameterMap().forEach((parameter, values) ->
                Collections.addAll(parameters.get(parameter), values));

        return parameters;
    }

    @Override
    public HttpRequest withBody() throws IOException {
        @Nullable final byte[] previous = (byte[]) getAttribute(Attributes.REQUEST_BODY);

        if (previous == null) {
            final ServletInputStream stream = getInputStream();
            this.body = ByteStreams.toByteArray(stream);
            setAttribute(Attributes.REQUEST_BODY, body);
        } else {
            this.body = previous;
        }

        return this;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return body == null ?
                super.getInputStream() :
                new ServletInputStreamAdapter(new ByteArrayInputStream(body));
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), getCharset()));
    }

    @Override
    public byte[] getBody() {
        return body;
    }

}
