package org.zalando.logbook.jaxws;

import java.io.IOException;
import java.util.Optional;

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;

public final class LocalResponse extends AbstractMessage implements HttpResponse {

    public LocalResponse(SOAPMessageContext context) {
        super(context);
    }

    @Override
    public Origin getOrigin() {
        return Origin.REMOTE;
    }
    
    @Override
    public int getStatus() {
        return Optional.ofNullable((Integer) context.get(MessageContext.HTTP_RESPONSE_CODE)).orElse(0);
    }

    @Override
    public HttpResponse withBody() throws IOException {
        state.updateAndGet(State::with);
        return this;
    }

    @Override
    public HttpResponse withoutBody() {
        state.updateAndGet(State::without);
        return this;
    }
}
