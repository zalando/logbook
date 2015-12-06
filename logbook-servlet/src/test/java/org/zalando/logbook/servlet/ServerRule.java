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

/*
 * ⁣​
 * Tracer: Servlet
 * ⁣⁣
 * Copyright (C) 2015 Zalando SE
 * ⁣⁣
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
 * ​⁣
 */

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import javax.servlet.Filter;
import java.net.InetSocketAddress;
import java.util.EnumSet;

import static java.lang.String.format;
import static javax.servlet.DispatcherType.ASYNC;
import static javax.servlet.DispatcherType.REQUEST;

public final class ServerRule extends TestWatcher {

    private final Server server;

    public ServerRule(final Filter... filters) {
        this.server = new Server(InetSocketAddress.createUnresolved("localhost", 0));

        final ServletContextHandler handler = new ServletContextHandler();
        handler.setContextPath("/");
        handler.addServlet(new ServletHolder(new EchoServlet()), "/echo");
        handler.addServlet(new ServletHolder(new ErrorServlet()), "/error");

        for (Filter filter : filters) {
            handler.addFilter(new FilterHolder(filter), "/*", EnumSet.of(REQUEST, ASYNC));
        }

        server.setHandler(handler);
    }

    public String url(final String path) {
        return server.getURI().resolve(path).toString();
    }

    @Override
    protected void starting(Description description) {
        try {
            server.start();
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected void finished(Description description) {
        try {
            server.stop();
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
