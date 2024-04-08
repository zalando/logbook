package org.zalando.logbook.httpclient5;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apiguardian.api.API;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Logbook.ResponseProcessingStage;

import java.io.IOException;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
@Slf4j
public final class LogbookHttpRequestInterceptor implements HttpRequestInterceptor {

    private final Logbook logbook;

    public LogbookHttpRequestInterceptor(final Logbook logbook) {
        this.logbook = logbook;
    }

    @Override
    public void process(HttpRequest httpRequest, EntityDetails entity, HttpContext context) throws HttpException, IOException {
        try {
            LocalRequest request = new LocalRequest(httpRequest, entity);
            final ResponseProcessingStage stage = logbook.process(request).write();
            context.setAttribute(Attributes.STAGE, stage);
        } catch (Exception e) {
            log.warn("Unable to log request. Will skip the request & response logging step.", e);
        }
    }
}
