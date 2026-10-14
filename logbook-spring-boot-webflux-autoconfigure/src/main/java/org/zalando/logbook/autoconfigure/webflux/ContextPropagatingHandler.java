package org.zalando.logbook.autoconfigure.webflux;

import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * Logbook logs outgoing requests on a Netty thread that has no knowledge of the request being handled, so the trace
 * id would be missing. Reactor Netty keeps the trace on the channel, so restore it for as long as Logbook logs.
 *
 * <p>Only write and read need it, as those are the only points Logbook logs at.
 */
final class ContextPropagatingHandler extends ChannelDuplexHandler {

    private static final ContextSnapshotFactory FACTORY = ContextSnapshotFactory.builder().build();

    private final ChannelDuplexHandler delegate;

    ContextPropagatingHandler(final ChannelDuplexHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public void write(
            final ChannelHandlerContext context,
            final Object message,
            final ChannelPromise promise) throws Exception {

        try (ContextSnapshot.Scope ignored = FACTORY.setThreadLocalsFrom(context.channel())) {
            delegate.write(context, message, promise);
        }
    }

    @Override
    public void channelRead(
            final ChannelHandlerContext context,
            final Object message) throws Exception {

        try (ContextSnapshot.Scope ignored = FACTORY.setThreadLocalsFrom(context.channel())) {
            delegate.channelRead(context, message);
        }
    }
}
