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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO make level configurable?
public final class DefaultHttpLogWriter implements HttpLogWriter {

    private final Logger logger;

    public DefaultHttpLogWriter() {
        this(LoggerFactory.getLogger("logbook"));
    }

    public DefaultHttpLogWriter(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean isActive(final RawHttpRequest request) {
        return logger.isTraceEnabled();
    }

    @Override
    public void writeRequest(final String request) {
        logger.trace("{}", request);
    }

    @Override
    public void writeResponse(final String response) {
        logger.trace("{}", response);
    }
}
