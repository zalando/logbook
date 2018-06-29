package org.zalando.logbook.jaxrs;

import org.zalando.logbook.Correlator;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.RawHttpRequest;

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
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.zalando.fauxpas.FauxPas.throwingConsumer;
import static org.zalando.logbook.jaxrs.Attributes.CORRELATOR;
import static org.zalando.logbook.jaxrs.Attributes.REQUEST;

@Provider
@ConstrainedTo(RuntimeType.CLIENT)
public final class LogbookClientFilter implements ClientRequestFilter, ClientResponseFilter, WriterInterceptor {

    private final Logbook logbook;

    public LogbookClientFilter(final Logbook logbook) {
        this.logbook = logbook;
    }

    @Override
    public void filter(final ClientRequestContext context) throws IOException {
        final RawHttpRequest request = new LocalRequest(context);

        if (context.hasEntity()) {
            context.setProperty(REQUEST, request);
        } else {
            logRequest(context::setProperty, request);
        }
    }

    @Override
    public void aroundWriteTo(final WriterInterceptorContext context) throws IOException, WebApplicationException {
        context.proceed();

        read(context::getProperty, REQUEST, RawHttpRequest.class)
                .ifPresent(throwingConsumer(request ->
                        logRequest(context::setProperty, request)));
    }

    private void logRequest(final BiConsumer<String, Correlator> context, final RawHttpRequest request)
            throws IOException {
        logbook.write(request)
                .ifPresent(correlator ->
                        context.accept(CORRELATOR, correlator));
    }

    @Override
    public void filter(final ClientRequestContext request, final ClientResponseContext response) {
        read(request::getProperty, CORRELATOR, Correlator.class)
                .ifPresent(throwingConsumer(correlator ->
                        correlator.write(new RemoteResponse(response))));
    }

    private static <T> Optional<T> read(final Function<String, Object> provider, final String name,
            final Class<T> type) {
        return Optional.ofNullable(provider.apply(name)).map(type::cast);
    }

}
