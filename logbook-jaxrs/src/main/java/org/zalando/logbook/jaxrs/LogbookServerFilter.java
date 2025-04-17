package org.zalando.logbook.jaxrs;

import jakarta.ws.rs.ConstrainedTo;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import lombok.AllArgsConstructor;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Logbook.RequestWritingStage;
import org.zalando.logbook.Logbook.ResponseProcessingStage;
import org.zalando.logbook.Logbook.ResponseWritingStage;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

import static org.zalando.fauxpas.FauxPas.throwingConsumer;

@Provider
@ConstrainedTo(RuntimeType.SERVER)
@AllArgsConstructor
public final class LogbookServerFilter implements ContainerRequestFilter, ContainerResponseFilter, WriterInterceptor {

    private final Logbook logbook;

    @Override
    public void filter(final ContainerRequestContext context) throws IOException {
        final RemoteRequest request = new RemoteRequest(context);
        final RequestWritingStage write = logbook.process(request);
        request.expose();
        final ResponseProcessingStage process = write.write();
        context.setProperty("process-response", process);
    }

    @Override
    public void filter(final ContainerRequestContext request, final ContainerResponseContext context) {
        final LocalResponse response = new LocalResponse(context);

        read(request::getProperty, "process-response", ResponseProcessingStage.class)
                .ifPresent(context.hasEntity() ?
                        throwingConsumer(stage -> {
                            request.setProperty("write-response", stage.process(response));
                            response.expose();
                        }) :
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
