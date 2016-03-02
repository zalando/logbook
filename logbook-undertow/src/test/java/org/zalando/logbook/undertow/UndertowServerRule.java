package org.zalando.logbook.undertow;

/*
 * #%L
 * Logbook: Undertow
 * %%
 * Copyright (C) 2016 Zalando SE
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

import static com.google.common.base.Preconditions.checkState;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.junit.rules.ExternalResource;

import com.google.common.base.Joiner;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.HttpString;

public class UndertowServerRule extends ExternalResource {

    private static final String LOCALHOST = "127.0.0.1";

    private HttpHandler handler = ResponseCodeHandler.HANDLE_404;
    private int port = -1;
    private Undertow undertow;

    public int getPort() {
        checkState(port > 0);
        return port;
    }

    public UndertowServerRule setHandler(final HttpHandler handler) {
        this.handler = requireNonNull(handler);
        return this;
    }

    public UndertowServerRule sendRequest(final HttpString method, final String requestTarget,
            final HeaderMap headers) throws IOException {

        final List<String> lines = new ArrayList<>();
        lines.add(String.format("%s %s HTTP/1.1", method, requestTarget));
        for (final HeaderValues headerValues : headers) {
            lines.add(headerValues.getHeaderName() + ": " + Joiner.on(',').join(headerValues));
        }

        try (final Socket s = new Socket(LOCALHOST, getPort())) {
            final OutputStream out = s.getOutputStream();
            out.write((Joiner.on("\r\n").join(lines) + "\r\n\r\n").getBytes(US_ASCII));
            out.flush();
            s.shutdownOutput();

            final InputStream in = s.getInputStream();
            do {
                in.skip(Long.MAX_VALUE);
            } while (in.read() != -1);
        }

        return this;
    }

    @Override
    protected void before() throws Throwable {
        final int newPort = findFreePort();
        final HttpHandler rootHandler = exchange -> handler.handleRequest(exchange);
        final Undertow newUndertow = Undertow.builder().addHttpListener(newPort, LOCALHOST, rootHandler).build();

        newUndertow.start();
        port = newPort;
        undertow = newUndertow;
    }

    @Override
    protected void after() {
        port = -1;
        final Undertow oldUndertow = undertow;
        if (oldUndertow != null) {
            undertow = null;
            oldUndertow.stop();
        }
    }

    private static int findFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
