package org.zalando.logbook.jaxrs;

import org.zalando.logbook.Correlator;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.RawHttpResponse;

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
import static org.zalando.logbook.jaxrs.Attributes.CORRELATOR;
import static org.zalando.logbook.jaxrs.Attributes.RESPONSE;

@Provider
@ConstrainedTo(RuntimeType.SERVER)
public final class LogbookServerFilter implements ContainerRequestFilter, ContainerResponseFilter, WriterInterceptor {

    private final Logbook logbook;

    public LogbookServerFilter(final Logbook logbook) {
        this.logbook = logbook;
    }

    @Override
    public void filter(final ContainerRequestContext context) throws IOException {
        logbook.write(new RemoteRequest(context))
                .ifPresent(correlator ->
                        context.setProperty(CORRELATOR, correlator));
    }

    @Override
    public void filter(final ContainerRequestContext request, final ContainerResponseContext response) {
        final RawHttpResponse rawHttpResponse = new LocalResponse(response);

        if (response.hasEntity()) {
            request.setProperty(RESPONSE, rawHttpResponse);
        } else {
            read(request::getProperty, CORRELATOR, Correlator.class)
                    .ifPresent(throwingConsumer(correlator ->
                            correlator.write(rawHttpResponse)));
        }
    }

    @Override
    public void aroundWriteTo(final WriterInterceptorContext context) throws IOException {
        context.proceed();

        read(context::getProperty, CORRELATOR, Correlator.class)
                .ifPresent(throwingConsumer(correlator ->
                        read(context::getProperty, RESPONSE, RawHttpResponse.class)
                                .ifPresent(throwingConsumer(correlator::write))));
    }

    private static <T> Optional<T> read(final Function<String, Object> provider, final String name,
            final Class<T> type) {
        return Optional.ofNullable(provider.apply(name)).map(type::cast);
    }

}
