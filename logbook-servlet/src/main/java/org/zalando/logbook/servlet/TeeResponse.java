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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.RawHttpResponse;

import javax.annotation.Nullable;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

final class TeeResponse extends HttpServletResponseWrapper implements RawHttpResponse, HttpResponse {

    private final HttpServletRequest request;
    private final ByteArrayDataOutput output = ByteStreams.newDataOutput();

    /**
     * Null until we a) capture it ourselves or b) retrieve it from {@link Attributes#RESPONSE_BODY}, which
     * was previously captured by another filter instance.
     */
    private byte[] body;

    TeeResponse(final HttpServletRequest request, final HttpServletResponse response) {
        super(response);
        this.request = request;
    }

    @Nullable
    private byte[] getAlreadyBufferedResponseBody() {
        return (byte[]) request.getAttribute(Attributes.RESPONSE_BODY);
    }

    private boolean isBuffering() {
        return isBuffering(this);
    }

    private boolean isNobodyBuffering() {
        return isBuffering(null);
    }

    private boolean isBuffering(@Nullable final Object buffer) {
        return request.getAttribute(Attributes.BUFFERING) == buffer;
    }

    private void setBuffering() {
        request.setAttribute(Attributes.BUFFERING, this);
    }

    @Override
    public Multimap<String, String> getHeaders() {
        final Multimap<String, String> headers = ArrayListMultimap.create();

        for (final String header : getHeaderNames()) {
            headers.putAll(header, getHeaders(header));
        }

        return headers;
    }

    @Override
    public Charset getCharset() {
        return Optional.ofNullable(getCharacterEncoding()).map(Charset::forName).orElse(ISO_8859_1);
    }

    @Override
    public HttpResponse withBody() {
        @Nullable final byte[] bufferedResponseBody = getAlreadyBufferedResponseBody();
        final boolean isAlreadyBuffered = bufferedResponseBody != null;

        if (isAlreadyBuffered) {
            setBody(bufferedResponseBody);
        } else {
            setBody(output.toByteArray());
        }

        return this;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new TeeServletOutputStream();
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(new OutputStreamWriter(getOutputStream(), getCharset()));
    }

    @Override
    public byte[] getBody() {
        return body;
    }

    private void setBody(byte[] body) {
        request.setAttribute(Attributes.RESPONSE_BODY, body);
        this.body = body;
    }

    @VisibleForTesting
    ByteArrayDataOutput getOutput() {
        return output;
    }

    private final class TeeServletOutputStream extends ServletOutputStream {

        private final OutputStream original;

        private TeeServletOutputStream() throws IOException {
            this.original = TeeResponse.super.getOutputStream();
        }

        @Override
        public void write(final int b) throws IOException {
            if (isNobodyBuffering()) {
                setBuffering();
                output.write(b);
            } else if (isBuffering()) {
                output.write(b);
            }

            original.write(b);
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            if (isNobodyBuffering()) {
                setBuffering();
                output.write(b, off, len);
            } else if (isBuffering()) {
                output.write(b, off, len);
            } else {
                "foo".toString();
            }

            original.write(b, off, len);
        }

    }

}
