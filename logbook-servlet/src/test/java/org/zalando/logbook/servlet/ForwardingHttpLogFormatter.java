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

import com.google.common.collect.ForwardingObject;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.Precorrelation;

import java.io.IOException;

class ForwardingHttpLogFormatter extends ForwardingObject implements HttpLogFormatter {

    private final HttpLogFormatter formatter;

    protected ForwardingHttpLogFormatter(final HttpLogFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    protected HttpLogFormatter delegate() {
        return formatter;
    }

    @Override
    public String format(final Precorrelation precorrelation) throws IOException {
        return delegate().format(precorrelation);
    }

    @Override
    public String format(final Correlation correlation) throws IOException {
        return delegate().format(correlation);
    }

}
