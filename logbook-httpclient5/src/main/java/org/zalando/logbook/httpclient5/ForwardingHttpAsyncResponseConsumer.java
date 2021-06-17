package org.zalando.logbook.httpclient5;

import lombok.experimental.Delegate;
import org.apache.hc.core5.http.nio.AsyncResponseConsumer;

abstract class ForwardingHttpAsyncResponseConsumer<T> implements AsyncResponseConsumer<T> {

    @Delegate
    protected abstract AsyncResponseConsumer<T> delegate();

}
