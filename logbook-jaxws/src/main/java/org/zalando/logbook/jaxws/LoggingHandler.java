package org.zalando.logbook.jaxws;

import java.io.IOException;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.zalando.logbook.Logbook;
import org.zalando.logbook.Logbook.ResponseProcessingStage;

public class LoggingHandler implements SOAPHandler<SOAPMessageContext> {

    private static final ThreadLocal<ResponseProcessingStage> STAGE_CONTEXT = new ThreadLocal<>();
    private Logbook logbook;

    public LoggingHandler(Logbook logbook) {
        this.logbook = logbook;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        boolean outbound = (boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        if (outbound) {
            LocalRequest request = new LocalRequest(context);
            try {
                ResponseProcessingStage process = logbook.process(request).write();
                STAGE_CONTEXT.set(process);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            ResponseProcessingStage stage = STAGE_CONTEXT.get();

            LocalResponse response = new LocalResponse(context);
            try {
                stage.process(response).write();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                STAGE_CONTEXT.remove();
            }
        }

        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext aContext) {
        STAGE_CONTEXT.remove();
        return false;
    }

    @Override
    public void close(MessageContext aContext) {
    }

    @Override
    public Set<QName> getHeaders() {
        return null;
    }
}
