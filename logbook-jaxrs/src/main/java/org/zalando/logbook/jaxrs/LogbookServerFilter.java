package org.zalando.logbook.jaxrs;

import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Logbook.ResponseProcessingStage;
import org.zalando.logbook.Logbook.ResponseWritingStage;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

import static org.zalando.fauxpas.FauxPas.throwingConsumer;

@Provider
@ConstrainedTo(RuntimeType.SERVER)
public final class LogbookServerFilter implements ContainerRequestFilter, ContainerResponseFilter, WriterInterceptor {

    private final Logbook logbook;

    public LogbookServerFilter(final Logbook logbook) {
        this.logbook = logbook;
    }

    @Override
    public void filter(final ContainerRequestContext context) throws IOException {
        final RemoteRequest request = new RemoteRequest(context);
        final ResponseProcessingStage stage = logbook.process(request).write();
        context.setProperty("process-response", stage);
    }

    @Override
    public void filter(final ContainerRequestContext request, final ContainerResponseContext context) {
        final HttpResponse response = new LocalResponse(context);

        read(request::getProperty, "process-response", ResponseProcessingStage.class)
                .ifPresent(context.hasEntity() ?
                        throwingConsumer(stage ->
                                request.setProperty("write-response", stage.process(response))) :
                        throwingConsumer(stage ->
                                stage.process(response).write()));
    }

    @Override
    public void aroundWriteTo(final WriterInterceptorContext context) throws IOException {
        context.proceed();

        read(context::getProperty, "write-response", ResponseWritingStage.class)
                .ifPresent(throwingConsumer(ResponseWritingStage::write));
    }

    private static <T> Optional<T> read(final Function<String, Object> provider, final String name,
            final Class<T> type) {
        return Optional.ofNullable(provider.apply(name)).map(type::cast);
    }

}
