package org.zalando.logbook;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;


public final class DefaultLogbookFactory implements LogbookFactory {

    @Override
    public Logbook create(
            @Nullable final Predicate<RawHttpRequest> condition,
            @Nullable final QueryFilter queryFilter,
            @Nullable final HeaderFilter headerFilter,
            @Nullable final BodyFilter bodyFilter,
            @Nullable final RequestFilter requestFilter,
            @Nullable final ResponseFilter responseFilter,
            @Nullable final HttpLogFormatter formatter,
            @Nullable final HttpLogWriter writer) {


        final HeaderFilter header = Optional.ofNullable(headerFilter).orElseGet(Filters::authorization);
        final BodyFilter body = Optional.ofNullable(bodyFilter).orElseGet(BodyFilter::none);

        return new DefaultLogbook(
                Optional.ofNullable(condition).orElse($ -> true),
                combine(queryFilter, header, body, requestFilter),
                combine(header, body, responseFilter),
                Optional.ofNullable(formatter).orElseGet(DefaultHttpLogFormatter::new),
                Optional.ofNullable(writer).orElseGet(DefaultHttpLogWriter::new)
        );
    }

    @Nonnull
    private RequestFilter combine(
            @Nullable final QueryFilter queryFilter,
            final HeaderFilter headerFilter,
            final BodyFilter bodyFilter,
            @Nullable final RequestFilter requestFilter) {

        final QueryFilter query = Optional.ofNullable(queryFilter).orElseGet(Filters::accessToken);

        return RequestFilter.merge(
                Optional.ofNullable(requestFilter).orElseGet(RequestFilter::none),
                request -> new FilteredHttpRequest(request, query, headerFilter, bodyFilter));
    }

    @Nonnull
    private ResponseFilter combine(
            final HeaderFilter headerFilter,
            final BodyFilter bodyFilter,
            @Nullable final ResponseFilter responseFilter) {

        return ResponseFilter.merge(
                Optional.ofNullable(responseFilter).orElseGet(ResponseFilter::none),
                response -> new FilteredHttpResponse(response, headerFilter, bodyFilter));
    }
}
