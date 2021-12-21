package org.zalando.logbook.jdkserver;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import lombok.AllArgsConstructor;
import org.apiguardian.api.API;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Strategy;

import java.io.IOException;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
@AllArgsConstructor
public final class LogbookFilter extends Filter {

    private final Logbook logbook;

    private final Strategy strategy;

    public LogbookFilter() {
        this(Logbook.create());
    }

    public LogbookFilter(final Logbook logbook) {
        this(logbook, null);
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        final Request request = new Request(exchange);
        final Response response = new Response(exchange);

        final Logbook.ResponseProcessingStage processing = process(request).write();
        final ForwardingHttpExchange forwardingHttpExchange = new ForwardingHttpExchange(response, exchange, processing);

        exchange.setStreams(request.getInputStream(), null);

        chain.doFilter(forwardingHttpExchange);

        forwardingHttpExchange.getResponseWritingStage().write();
    }

    @Override
    public String description() {
        return "Logbook filter";
    }

    private Logbook.RequestWritingStage process(
            final HttpRequest request) throws IOException {

        return strategy == null ?
                logbook.process(request) :
                logbook.process(request, strategy);
    }

}
