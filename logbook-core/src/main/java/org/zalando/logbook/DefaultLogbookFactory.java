package org.zalando.logbook;

import org.apiguardian.api.API;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(status = INTERNAL)
public final class DefaultLogbookFactory implements LogbookFactory {

    @Override
    public Logbook create(
            @Nullable final Predicate<HttpRequest> nullableCondition,
            @Nullable final QueryFilter queryFilter,
            @Nullable final HeaderFilter headerFilter,
            @Nullable final BodyFilter bodyFilter,
            @Nullable final RequestFilter requestFilter,
            @Nullable final ResponseFilter responseFilter,
            @Nullable final Strategy strategy,
            @Nullable final Sink sink) {

        final Predicate<HttpRequest> condition = Optional.ofNullable(nullableCondition)
                .orElse($ -> true);

        final HeaderFilter header = Optional.ofNullable(headerFilter)
                .orElseGet(HeaderFilters::defaultValue);

        final BodyFilter body = Optional.ofNullable(bodyFilter)
                .orElseGet(BodyFilters::defaultValue);

        return new DefaultLogbook(
                condition,
                combine(queryFilter, header, body, requestFilter),
                combine(header, body, responseFilter),
                Optional.ofNullable(strategy).orElseGet(DefaultStrategy::new),
                Optional.ofNullable(sink).orElseGet(() ->
                        new DefaultSink(
                                new DefaultHttpLogFormatter(),
                                new DefaultHttpLogWriter()
                        ))
        );
    }

    @Nonnull
    private RequestFilter combine(
            @Nullable final QueryFilter queryFilter,
            final HeaderFilter headerFilter,
            final BodyFilter bodyFilter,
            @Nullable final RequestFilter requestFilter) {

        final QueryFilter query = Optional.ofNullable(queryFilter).orElseGet(QueryFilters::defaultValue);

        return RequestFilter.merge(
                Optional.ofNullable(requestFilter).orElseGet(RequestFilters::defaultValue),
                request -> new FilteredHttpRequest(request, query, headerFilter, bodyFilter));
    }

    @Nonnull
    private ResponseFilter combine(
            final HeaderFilter headerFilter,
            final BodyFilter bodyFilter,
            @Nullable final ResponseFilter responseFilter) {

        return ResponseFilter.merge(
                Optional.ofNullable(responseFilter).orElseGet(ResponseFilters::defaultValue),
                response -> new FilteredHttpResponse(response, headerFilter, bodyFilter));
    }
}
