package org.zalando.logbook.jaxws;

import org.mockito.ArgumentCaptor;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.Sink;
import org.zalando.logbook.TestStrategy;

import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

final class Infrastructure {

    private static final URL WSDL;
    private static final QName SERVICE_NAME =
            new QName("http://jaxws.logbook.zalando.org/", "BookServiceImplService");

    static {
        try {
            WSDL = new URL("file:src/test/resources/books.wsdl");
        } catch (final MalformedURLException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final Endpoint server = Endpoint.create(new BookServiceImpl());
    private final BookService client = Service.create(WSDL, SERVICE_NAME).getPort(BookService.class);
    private final Sink sink = mock(Sink.class);

    private final Logbook logbook = Logbook.builder()
            .strategy(new TestStrategy())
            .sink(sink)
            .build();

    public Infrastructure() {
        when(sink.isActive()).thenReturn(true);
    }

    void start() {
        server.publish("http://localhost:8080/books");
    }

    Endpoint server() {
        return server;
    }

    @SuppressWarnings("unchecked")
    <Client extends BookService & BindingProvider> Client client() {
        return (Client) client;
    }

    void register(final Binding binding, final Function<Logbook, Handler<SOAPMessageContext>> handlerFunction) {
        @SuppressWarnings("rawtypes") final List<Handler> handlers = binding.getHandlerChain();
        handlers.add(handlerFunction.apply(logbook));
        binding.setHandlerChain(handlers);
    }

    HttpRequest request() {
        final ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);

        try {
            verify(sink).write(any(Precorrelation.class), captor.capture());
        } catch (final IOException e) {
            throw new AssertionError(e);
        }

        return captor.getValue();
    }

    HttpResponse response()  {
        final ArgumentCaptor<HttpResponse> captor = ArgumentCaptor.forClass(HttpResponse.class);

        try {
            verify(sink).write(any(Correlation.class), any(HttpRequest.class), captor.capture());
        } catch (final IOException e) {
            throw new AssertionError(e);
        }

        return captor.getValue();
    }

    void stop() {
        server.stop();
    }

}
