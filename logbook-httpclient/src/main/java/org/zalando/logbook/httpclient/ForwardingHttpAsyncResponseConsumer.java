package org.zalando.logbook.httpclient;

import lombok.experimental.Delegate;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;

abstract class ForwardingHttpAsyncResponseConsumer<T> implements HttpAsyncResponseConsumer<T> {

    @Delegate
    protected abstract HttpAsyncResponseConsumer<T> delegate();

}
