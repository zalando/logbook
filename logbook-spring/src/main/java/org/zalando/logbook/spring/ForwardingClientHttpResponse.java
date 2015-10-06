package org.zalando.logbook.spring;

/*
 * #%L
 * Logbook: Spring
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.InputStream;

public abstract class ForwardingClientHttpResponse extends ForwardingObject implements ClientHttpResponse {
    
    @Override
    protected abstract ClientHttpResponse delegate();

    @Override
    public HttpStatus getStatusCode() throws IOException {
        return delegate().getStatusCode();
    }

    @Override
    public void close() {
        delegate().close();
    }

    @Override
    public InputStream getBody() throws IOException {
        return delegate().getBody();
    }

    @Override
    public String getStatusText() throws IOException {
        return delegate().getStatusText();
    }

    @Override
    public HttpHeaders getHeaders() {
        return delegate().getHeaders();
    }

    @Override
    public int getRawStatusCode() throws IOException {
        return delegate().getRawStatusCode();
    }

}
