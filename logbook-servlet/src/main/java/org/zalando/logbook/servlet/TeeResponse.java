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


import org.zalando.logbook.*;

import javax.annotation.Nullable;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.zalando.logbook.Util.firstNonNull;

final class TeeResponse extends HttpServletResponseWrapper implements RawHttpResponse, HttpResponse {

    private final HttpServletRequest request;
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    private final TeeServletOutputStream stream;
    private final PrintWriter writer;

    /**
     * Null until we successfully intercepted it.
     */
    @Nullable
    private byte[] body;

    TeeResponse(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        super(response);
        this.request = request;
        this.stream = new TeeServletOutputStream();
        this.writer = new TeePrintWriter();
    }

    @Override
    public Origin getOrigin() {
        return Origin.LOCAL;
    }

    @Override
    public Multimap<String, String> getHeaders() {
        final Multimap<String, String> headers = Multimaps.immutableOf();

        for (final String header : getHeaderNames()) {
            headers.put(header, getHeaders(header));
        }

        return headers;
    }

    @Override
    public String getContentType() {
        return firstNonNull(super.getContentType(), "");
    }

    @Override
    public Charset getCharset() {
        return Optional.ofNullable(getCharacterEncoding()).map(Charset::forName).orElse(ISO_8859_1);
    }

    @Override
    public HttpResponse withBody() {
        this.body = output.toByteArray();
        return this;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return stream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return writer;
    }

    @Override
    public byte[] getBody() {
        return body;
    }

    ByteArrayOutputStream getOutput() {
        return output;
    }

    private final class TeePrintWriter extends PrintWriter {

        public TeePrintWriter() {
            super(new OutputStreamWriter(stream, getCharset()));
        }

    }

    private final class TeeServletOutputStream extends ServletOutputStream {

        private final OutputStream original;

        private TeeServletOutputStream() throws IOException {
            this.original = TeeResponse.super.getOutputStream();
        }

        @Override
        public void write(final int b) throws IOException {
            output.write(b);
            original.write(b);
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            output.write(b, off, len);
            original.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            original.flush();
        }

        @Override
        public void close() throws IOException {
            original.close();
        }

    }

}
