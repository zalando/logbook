package org.zalando.logbook.jaxws;

import com.sun.net.httpserver.HttpExchange;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.net.URI;
import java.util.Optional;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;
import static javax.xml.ws.handler.MessageContext.SERVLET_REQUEST;

interface Accessor {

    String getRemote();
    String getProtocolVersion();
    String getMethod();
    String getScheme();
    String getHost();
    Optional<Integer> getPort();
    String getPath();
    String getQuery();

    static Accessor create(final SOAPMessageContext context) {
        if (context.containsKey("com.sun.xml.ws.http.exchange")) {
            return new HttpExchangeAccessor((HttpExchange) context.get("com.sun.xml.ws.http.exchange"));
        } else if (context.containsKey("com.sun.xml.internal.ws.http.exchange")) {
            return new HttpExchangeAccessor((HttpExchange) context.get("com.sun.xml.internal.ws.http.exchange"));
        } else if (context.containsKey(SERVLET_REQUEST)) {
            return new HttpServletRequestAccessor((HttpServletRequest) context.get(SERVLET_REQUEST));
        } else if (context.containsKey(ENDPOINT_ADDRESS_PROPERTY)) {
            return new EndpointAddressAccessor(URI.create(context.get(ENDPOINT_ADDRESS_PROPERTY).toString()));
        } else {
            throw new UnsupportedOperationException("Unsupported JAX-WS environment");
        }
    }

}
