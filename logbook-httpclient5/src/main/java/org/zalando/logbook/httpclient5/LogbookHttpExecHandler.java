package org.zalando.logbook.httpclient5;

import org.apache.hc.client5.http.classic.ExecChain;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.zalando.logbook.Logbook;

import java.io.IOException;

public class LogbookHttpExecHandler implements ExecChainHandler {

    private final Logbook logbook;

    public LogbookHttpExecHandler(Logbook logbook) {
        this.logbook = logbook;
    }

    @Override
    public ClassicHttpResponse execute(ClassicHttpRequest request, ExecChain.Scope scope, ExecChain execChain) throws IOException, HttpException {
        LocalRequest localRequest = new LocalRequest(request, request.getEntity());
        Logbook.ResponseProcessingStage stage = logbook.process(localRequest).write();

        ClassicHttpResponse response = execChain.proceed(request, scope);

        stage.process(new RemoteResponse(response)).write();

        return response;
    }
}
