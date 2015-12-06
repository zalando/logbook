package org.zalando.logbook.servlet;

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

import org.zalando.logbook.Logbook;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class LogbookFilter implements HttpFilter {

    private final Logbook logbook;
    private final Strategy strategy;

    public LogbookFilter() {
        this(Logbook.create());
    }

    public LogbookFilter(final Logbook logbook) {
        this(logbook, Strategy.NORMAL);
    }

    public LogbookFilter(final Logbook logbook, final Strategy strategy) {
        this.logbook = logbook;
        this.strategy = strategy;
    }

    @Override
    public void doFilter(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse,
            final FilterChain chain) throws ServletException, IOException {

        strategy.doFilter(logbook, httpRequest, httpResponse, chain);
    }

}
