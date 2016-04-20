package org.zalando.logbook;

/*
 * #%L
 * Logbook: Core
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
import com.google.common.collect.ListMultimap;

import java.io.IOException;
import java.nio.charset.Charset;

public abstract class ForwardingRawHttpRequest extends ForwardingObject implements RawHttpRequest {

    @Override
    protected abstract RawHttpRequest delegate();

    @Override
    public ListMultimap<String, String> getHeaders() {
        return delegate().getHeaders();
    }

    @Override
    public String getContentType() {
        return delegate().getContentType();
    }

    @Override
    public Charset getCharset() {
        return delegate().getCharset();
    }

    @Override
    public Origin getOrigin() {
        return delegate().getOrigin();
    }

    @Override
    public String getRemote() {
        return delegate().getRemote();
    }

    @Override
    public String getMethod() {
        return delegate().getMethod();
    }

    @Override
    public String getRequestUri() {
        return delegate().getRequestUri();
    }

    @Override
    public ListMultimap<String, String> getQueryParameters() {
        return delegate().getQueryParameters();
    }

    @Override
    public String getProtocolVersion() {
        return delegate().getProtocolVersion();
    }

    @Override
    public HttpRequest withBody() throws IOException {
        return delegate().withBody();
    }

}
