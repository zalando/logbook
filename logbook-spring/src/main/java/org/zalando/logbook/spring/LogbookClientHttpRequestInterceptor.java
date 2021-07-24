package org.zalando.logbook.spring;

import lombok.AllArgsConstructor;
import org.apiguardian.api.API;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Logbook;

import java.io.IOException;

@API(status = API.Status.EXPERIMENTAL)
@AllArgsConstructor
public final class LogbookClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private final Logbook logbook;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        final org.zalando.logbook.HttpRequest httpRequest = new LocalRequest(request, body);
        final Logbook.ResponseProcessingStage stage = logbook.process(httpRequest).write();

        ClientHttpResponse response = new BufferingClientHttpResponseWrapper(execution.execute(request, body));

        final HttpResponse httpResponse = new RemoteResponse(response);
        stage.process(httpResponse).write();

        return response;
    }
}
