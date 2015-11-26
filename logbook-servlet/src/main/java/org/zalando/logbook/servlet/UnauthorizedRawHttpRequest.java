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

import org.zalando.logbook.ForwardingRawHttpRequest;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.RawHttpRequest;

import java.io.IOException;

final class UnauthorizedRawHttpRequest extends ForwardingRawHttpRequest {

    private final TeeRequest request;

    UnauthorizedRawHttpRequest(TeeRequest request) {
        this.request = request;
    }

    @Override
    protected TeeRequest delegate() {
        return request;
    }

    @Override
    public HttpRequest withBody() throws IOException {
        return new UnauthorizedHttpRequest(delegate());
    }

}
