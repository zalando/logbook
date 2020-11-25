package org.zalando.logbook.jaxws;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;

import javax.xml.ws.handler.soap.SOAPMessageContext;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class HttpMessageContext implements SOAPMessageContext, Accessor {

    @Delegate
    private final SOAPMessageContext context;

    @Delegate
    private final Accessor accessor;

    public HttpMessageContext(final SOAPMessageContext context) {
        this(context, Accessor.create(context));
    }

}
