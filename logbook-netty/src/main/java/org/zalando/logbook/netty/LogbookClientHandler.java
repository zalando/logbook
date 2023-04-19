package org.zalando.logbook.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import lombok.RequiredArgsConstructor;
import org.apiguardian.api.API;
import org.zalando.logbook.api.Logbook;

import javax.annotation.concurrent.NotThreadSafe;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.zalando.fauxpas.FauxPas.throwingRunnable;
import static org.zalando.logbook.api.Logbook.RequestWritingStage;
import static org.zalando.logbook.api.Logbook.ResponseWritingStage;
import static org.zalando.logbook.api.Origin.LOCAL;
import static org.zalando.logbook.api.Origin.REMOTE;
import static org.zalando.logbook.netty.Conditionals.runIf;

@API(status = EXPERIMENTAL)
@NotThreadSafe
@RequiredArgsConstructor
public final class LogbookClientHandler extends ChannelDuplexHandler {

    private final Sequence sequence = new Sequence(2);

    private final Logbook logbook;

    private Request request;
    private Response response;
    private RequestWritingStage requestStage;
    private ResponseWritingStage responseStage;

    @Override
    public void write(
            final ChannelHandlerContext context,
            final Object message,
            final ChannelPromise promise) {

        runIf(message, HttpRequest.class, httpRequest -> {
            this.request = new Request(context, LOCAL, httpRequest);
            this.requestStage = logbook.process(request);
        });

        runIf(message, HttpContent.class, content -> request.buffer(content.content()));
        runIf(message, ByteBuf.class, request::buffer);

        runIf(message, LastHttpContent.class, content ->
                sequence.set(0, throwingRunnable(requestStage::write)));

        context.write(message, promise);
    }

    @Override
    public void channelRead(
            final ChannelHandlerContext context,
            final Object message) {

        runIf(message, HttpResponse.class, httpResponse -> {
            this.response = new Response(REMOTE, httpResponse);
            this.responseStage = requestStage.process(response);
        });

        runIf(message, HttpContent.class, content -> response.buffer(content.content()));
        runIf(message, ByteBuf.class, response::buffer);

        runIf(message, LastHttpContent.class, content ->
                sequence.set(1, throwingRunnable(responseStage::write)));

        context.fireChannelRead(message);
    }

}
