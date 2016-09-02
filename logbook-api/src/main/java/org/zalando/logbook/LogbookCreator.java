package org.zalando.logbook;

import lombok.Singular;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public final class LogbookCreator {

    LogbookCreator() {
        // package private so we can trick code coverage
    }

    @lombok.Builder(builderClassName = "Builder")
    private static Logbook create(
            @Nullable final Predicate<RawHttpRequest> condition,
            @Singular final List<QueryFilter> queryFilters,
            @Singular final List<HeaderFilter> headerFilters,
            @Singular final List<BodyFilter> bodyFilters,
            @Singular final List<RequestFilter> requestFilters,
            @Singular final List<ResponseFilter> responseFilters,
            @Nullable final HttpLogFormatter formatter,
            @Nullable final HttpLogWriter writer) {

        final LogbookFactory factory = LogbookFactory.INSTANCE;

        final QueryFilter queryFilter = queryFilters.stream()
                .reduce(QueryFilter::merge)
                .orElse(null);

        final HeaderFilter headerFilter = headerFilters.stream()
                .reduce(HeaderFilter::merge)
                .orElse(null);

        final BodyFilter bodyFilter = bodyFilters.stream()
                .reduce(BodyFilter::merge)
                .orElse(null);

        final RequestFilter requestFilter = requestFilters.stream()
                .reduce(RequestFilter::merge)
                .orElse(null);

        final ResponseFilter responseFilter = responseFilters.stream()
                .reduce(ResponseFilter::merge)
                .orElse(null);

        return factory.create(
                condition,
                queryFilter,
                headerFilter,
                bodyFilter,
                requestFilter,
                responseFilter,
                formatter,
                writer);
    }

}
