package org.zalando.logbook.httpclient;

/*
 * #%L
 * Logbook: HTTP Client
 * %%
 * Copyright (C) 2015 - 2016 Zalando SE
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

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

abstract class ForwardingHttpAsyncResponseConsumer<T> implements HttpAsyncResponseConsumer<T> {

    protected abstract HttpAsyncResponseConsumer<T> delegate();

    @Override
    public void responseReceived(final HttpResponse response) throws IOException, HttpException {
        delegate().responseReceived(response);
    }

    @Override
    public void consumeContent(final ContentDecoder decoder, final IOControl control) throws IOException {
        delegate().consumeContent(decoder, control);
    }

    @Override
    public void responseCompleted(final HttpContext context) {
        delegate().responseCompleted(context);
    }

    @Override
    public boolean cancel() {
        return delegate().cancel();
    }

    @Override
    public boolean isDone() {
        return delegate().isDone();
    }

    @Override
    public T getResult() {
        return delegate().getResult();
    }

    @Override
    public void failed(final Exception e) {
        delegate().failed(e);
    }

    @Override
    public Exception getException() {
        return delegate().getException();
    }

    @Override
    public void close() throws IOException {
        delegate().close();
    }

}
