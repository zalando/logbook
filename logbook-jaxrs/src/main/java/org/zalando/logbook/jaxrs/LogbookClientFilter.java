package org.zalando.logbook.jaxrs;

import org.zalando.logbook.Logbook;
import org.zalando.logbook.Logbook.RequestWritingStage;
import org.zalando.logbook.Logbook.ResponseProcessingStage;

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
public final class LogbookClientFilter implements ClientRequestFilter, ClientResponseFilter, WriterInterceptor {

    private final Logbook logbook;

    public LogbookClientFilter(final Logbook logbook) {
        this.logbook = logbook;
    }

    @Override
    public void filter(final ClientRequestContext context) throws IOException {
        final RequestWritingStage stage = logbook.process(new LocalRequest(context));

        if (context.hasEntity()) {
            context.setProperty("write-request", stage);
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
    public void filter(final ClientRequestContext request, final ClientResponseContext response) {
        read(request::getProperty, "process-response", ResponseProcessingStage.class)
                .ifPresent(throwingConsumer(correlator ->
                        correlator.process(new RemoteResponse(response)).write()));
    }

    private static <T> Optional<T> read(final Function<String, Object> provider, final String name,
            final Class<T> type) {
        return Optional.ofNullable(provider.apply(name)).map(type::cast);
    }

}
