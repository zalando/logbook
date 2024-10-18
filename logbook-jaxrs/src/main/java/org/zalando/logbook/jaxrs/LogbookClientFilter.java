package org.zalando.logbook.jaxrs;

import jakarta.ws.rs.ConstrainedTo;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
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
@ConstrainedTo(RuntimeType.CLIENT)
@AllArgsConstructor
public final class LogbookClientFilter implements ClientRequestFilter, ClientResponseFilter, WriterInterceptor {

    private final Logbook logbook;

    @Override
    public void filter(final ClientRequestContext context) throws IOException {
        final LocalRequest request = new LocalRequest(context);
        final RequestWritingStage stage = logbook.process(request);

        if (context.hasEntity()) {
            context.setProperty("request", request);
            context.setProperty("write-request", stage);
            request.expose();
        } else {
            context.setProperty("process-response", stage.write());
        }
    }

    @Override
    public void aroundWriteTo(final WriterInterceptorContext context) throws IOException, WebApplicationException {
        context.proceed();

        read(context::getProperty, "write-request", RequestWritingStage.class)
                .ifPresent(throwingConsumer(stage ->
                        context.setProperty("process-response", stage.write())));
    }

    @Override
    public void filter(final ClientRequestContext request, final ClientResponseContext context) {
        read(request::getProperty, "process-response", ResponseProcessingStage.class)
                .ifPresent(throwingConsumer(stage -> {
                    final RemoteResponse response = new RemoteResponse(context);
                    final ResponseWritingStage write = stage.process(response);
                    response.expose();
                    write.write();
                }));
    }

    private static <T> Optional<T> read(
            final Function<String, Object> provider,
            final String name,
            final Class<T> type) {

        return Optional.ofNullable(provider.apply(name)).map(type::cast);
    }

}
