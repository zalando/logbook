package org.zalando.logbook.httpclient5;

import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apiguardian.api.API;
import org.zalando.logbook.api.Logbook;
import org.zalando.logbook.api.Logbook.ResponseProcessingStage;

import java.io.IOException;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
public final class LogbookHttpRequestInterceptor implements HttpRequestInterceptor {

    private final Logbook logbook;

    public LogbookHttpRequestInterceptor(final Logbook logbook) {
        this.logbook = logbook;
    }

    @Override
    public void process(HttpRequest httpRequest, EntityDetails entity, HttpContext context) throws HttpException, IOException {
        LocalRequest request = new LocalRequest(httpRequest, entity);
        final ResponseProcessingStage stage = logbook.process(request).write();
        context.setAttribute(Attributes.STAGE, stage);
    }
}
