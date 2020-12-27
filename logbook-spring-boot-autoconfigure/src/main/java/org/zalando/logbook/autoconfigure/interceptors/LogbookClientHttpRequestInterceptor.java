package org.zalando.logbook.autoconfigure.interceptors;

import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apiguardian.api.API;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.zalando.logbook.httpclient.LogbookHttpRequestInterceptor;
import org.zalando.logbook.httpclient.LogbookHttpResponseInterceptor;

import java.io.IOException;

/**
 * Uses {@link LogbookHttpResponseInterceptor} and {@link LogbookHttpResponseInterceptor} to log the start and
 * completion of client http requests.
 */
@API(status = API.Status.EXPERIMENTAL)
public class LogbookClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private final LogbookHttpRequestInterceptor requestInterceptor;
    private final LogbookHttpResponseInterceptor responseInterceptor;

    public LogbookClientHttpRequestInterceptor(LogbookHttpRequestInterceptor requestInterceptor, LogbookHttpResponseInterceptor responseInterceptor) {
        this.requestInterceptor = requestInterceptor;
        this.responseInterceptor = responseInterceptor;
    }

    static class RequestAdapter extends BasicHttpRequest {
        static RequestAdapter adapt(HttpRequest request) {
            return new RequestAdapter(request.getMethod().name(), request.getURI().toString());
        }

        private RequestAdapter(String method, String uri) {
            super(method, uri);
        }
    }

    static class ResponseAdapter extends BasicHttpResponse {
        static ResponseAdapter adapt(ProtocolVersion version, ClientHttpResponse response) throws IOException {
            return new ResponseAdapter(version, response.getRawStatusCode(), response.getStatusText());
        }

        private ResponseAdapter(ProtocolVersion version, int code, String status) {
            super(version, code, status);
        }
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        RequestAdapter adaptedRequest = RequestAdapter.adapt(request);
        HttpContext ctx = new BasicHttpContext();

        requestInterceptor.process(adaptedRequest, ctx);

        ClientHttpResponse response = execution.execute(request, body);
        ResponseAdapter adaptedResponse = ResponseAdapter.adapt(adaptedRequest.getProtocolVersion(), response);

        responseInterceptor.process(adaptedResponse, ctx);

        return response;
    }
}
