package org.zalando.logbook.autoconfigure.webflux;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.netty.LogbookClientHandler;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ContextPropagatingHandlerTest {

    private final Logbook logbook = Logbook.create();

    @Test
    void wrapsTheHandlerWhenContextPropagationIsPresent() {
        assertThat(LogbookNettyClientCustomizer.createHandler(logbook, true))
                .isInstanceOf(ContextPropagatingHandler.class)
                .extracting("delegate")
                .isInstanceOf(LogbookClientHandler.class);
    }

    @Test
    void usesThePlainHandlerWhenContextPropagationIsAbsent() {
        assertThat(LogbookNettyClientCustomizer.createHandler(logbook, false))
                .isExactlyInstanceOf(LogbookClientHandler.class);
    }

    @Test
    void passesWriteAndReadToTheDelegate() {
        final RecordingHandler delegate = new RecordingHandler();
        final EmbeddedChannel channel = new EmbeddedChannel(new ContextPropagatingHandler(delegate));

        channel.writeOutbound("request");
        channel.writeInbound("response");

        assertThat(delegate.written).isEqualTo("request");
        assertThat(delegate.read).isEqualTo("response");
    }

    // forwarding only two methods is safe for as long as the delegate overrides nothing else
    @Test
    void delegateOverridesNothingBeyondWriteAndChannelRead() {
        assertThat(Arrays.stream(LogbookClientHandler.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .map(Method::getName))
                .containsExactlyInAnyOrder("write", "channelRead");
    }

    private static final class RecordingHandler extends ChannelDuplexHandler {

        private Object written;
        private Object read;

        @Override
        public void write(final ChannelHandlerContext context, final Object message, final ChannelPromise promise) {
            this.written = message;
            context.write(message, promise);
        }

        @Override
        public void channelRead(final ChannelHandlerContext context, final Object message) {
            this.read = message;
            context.fireChannelRead(message);
        }
    }
}
