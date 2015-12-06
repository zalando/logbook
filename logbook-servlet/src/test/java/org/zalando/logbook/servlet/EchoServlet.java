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

import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EchoServlet extends HttpServlet {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        if ("true".equals(request.getParameter("async"))) {
            request.startAsync(request, response);
            executor.submit(() -> copy(request, response));
        } else {
            copy(request, response);
        }
    }

    private Void copy(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final String contentType = request.getContentType();
        if (contentType != null) {
            response.setHeader("Content-Type", contentType);
        }

        switch (Strings.nullToEmpty(request.getParameter("mode"))) {
            case "writer": {
                final BufferedReader input = request.getReader();
                final PrintWriter putput = response.getWriter();

                CharStreams.copy(input, putput);
                break;
            }
            case "byte": {
                final ServletInputStream input = request.getInputStream();
                final ServletOutputStream output = response.getOutputStream();

                while (true) {
                    final int read = input.read();
                    if (read == -1) {
                        break;
                    }
                    output.write(read);
                }
                break;
            }
            case "bytes": {
                final ServletInputStream input = request.getInputStream();
                final ServletOutputStream output = response.getOutputStream();

                final byte[] buffer = new byte[1];

                while (true) {
                    final int read = input.read(buffer);
                    if (read == -1) {
                        break;
                    }
                    output.write(buffer);
                }
                break;
            }
            default: {
                final ServletInputStream input = request.getInputStream();
                final ServletOutputStream output = response.getOutputStream();

                ByteStreams.copy(input, output);
                break;
            }
        }

        return null;
    }

}
