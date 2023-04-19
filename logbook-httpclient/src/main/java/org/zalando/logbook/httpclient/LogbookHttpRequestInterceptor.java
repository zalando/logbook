package org.zalando.logbook.httpclient;

import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.apiguardian.api.API;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Logbook.ResponseProcessingStage;

import java.io.IOException;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public final class LogbookHttpRequestInterceptor implements HttpRequestInterceptor {

    private final Logbook logbook;

    public LogbookHttpRequestInterceptor(final Logbook logbook) {
        this.logbook = logbook;
    }

    @Override
    public void process(final HttpRequest httpRequest, final HttpContext context) throws IOException {
        final LocalRequest request = new LocalRequest(httpRequest);
        final ResponseProcessingStage stage = logbook.process(request).write();
        context.setAttribute(Attributes.STAGE, stage);
    }

}
