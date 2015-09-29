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

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.google.common.base.MoreObjects.firstNonNull;

final class DefaultTeeHttpServletRequest extends HttpServletRequestWrapper implements TeeHttpServletRequest {

    private final byte[] content;

    public DefaultTeeHttpServletRequest(final HttpServletRequest request, final byte[] content) {
        super(request);
        this.content = content;
    }

    @Override
    public ServletInputStream getInputStream() {
        return new ServletInputStreamAdapter(new ByteArrayInputStream(content));
    }

    @Override
    public byte[] getBodyAsByteArray() {
        return content;
    }

    @Override
    public String getCharacterEncoding() {
        return firstNonNull(super.getCharacterEncoding(), "ISO-8859-1"); // TODO constant
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), getCharacterEncoding()));
    }

    private final class ServletInputStreamAdapter extends ServletInputStream {

        private final InputStream stream;

        public ServletInputStreamAdapter(final InputStream stream) {
            this.stream = stream;
        }

        @Override
        public int read() throws IOException {
            return stream.read();
        }

        @Override
        public int read(final byte[] b) throws IOException {
            return stream.read(b);
        }

        @Override
        public int read(final byte[] b, final int off, final int len) throws IOException {
            return stream.read(b, off, len);
        }

    }

}
