package org.zalando.logbook.jaxrs;

import lombok.AllArgsConstructor;
import org.zalando.logbook.api.Logbook;
import org.zalando.logbook.api.Logbook.RequestWritingStage;
import org.zalando.logbook.api.Logbook.ResponseProcessingStage;
import org.zalando.logbook.api.Logbook.ResponseWritingStage;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
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
        } else {
            context.setProperty("process-response", stage.write());
        }

        request.expose();
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
