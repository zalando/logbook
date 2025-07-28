package org.zalando.logbook.httpclient;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.apiguardian.api.API;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Logbook.ResponseProcessingStage;

import java.io.IOException;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
@Slf4j
public final class LogbookHttpRequestInterceptor implements HttpRequestInterceptor {

    private final Logbook logbook;

    public LogbookHttpRequestInterceptor(final Logbook logbook) {
        this.logbook = logbook;
    }

    @Override
    public void process(final HttpRequest httpRequest, final HttpContext context) throws IOException {
        try {
            final LocalRequest request = new LocalRequest(httpRequest, context);
            final ResponseProcessingStage stage = logbook.process(request).write();
            context.setAttribute(Attributes.STAGE, stage);
        } catch (Exception e) {
            log.warn("Unable to log request. Will skip the request & response logging step.", e);
        }
    }

}
