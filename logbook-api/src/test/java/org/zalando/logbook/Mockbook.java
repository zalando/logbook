package org.zalando.logbook;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;

final class Mockbook implements Logbook {

    private final Predicate<RawHttpRequest> predicate;
    private final QueryFilter queryFilter;
    private final HeaderFilter headerFilter;
    private final BodyFilter bodyFilter;
    private final RequestFilter requestFilter;
    private final ResponseFilter responseFilter;
    private final HttpLogFormatter formatter;
    private final HttpLogWriter writer;

    public Mockbook(
            @Nullable final Predicate<RawHttpRequest> predicate,
            @Nullable final QueryFilter queryFilter,
            @Nullable final HeaderFilter headerFilter,
            @Nullable final BodyFilter bodyFilter,
            @Nullable final RequestFilter requestFilter,
            @Nullable final ResponseFilter responseFilter,
            @Nullable final HttpLogFormatter formatter,
            @Nullable final HttpLogWriter writer) {
        this.predicate = predicate;
        this.queryFilter = queryFilter;
        this.headerFilter = headerFilter;
        this.bodyFilter = bodyFilter;
        this.requestFilter = requestFilter;
        this.responseFilter = responseFilter;
        this.formatter = formatter;
        this.writer = writer;
    }

    @Override
    public Optional<Correlator> write(final RawHttpRequest request) throws IOException {
        throw new UnsupportedOperationException();
    }

    public Predicate<RawHttpRequest> getPredicate() {
        return predicate;
    }

    public BodyFilter getBodyFilter() {
        return bodyFilter;
    }

    public HeaderFilter getHeaderFilter() {
        return headerFilter;
    }

    public QueryFilter getQueryFilter() {
        return queryFilter;
    }

    public RequestFilter getRequestFilter() {
        return requestFilter;
    }

    public ResponseFilter getResponseFilter() {
        return responseFilter;
    }

    public HttpLogFormatter getFormatter() {
        return formatter;
    }

    public HttpLogWriter getWriter() {
        return writer;
    }

}
