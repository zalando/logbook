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
            @Singular final List<RawRequestFilter> rawRequestFilters,
            @Singular final List<RawResponseFilter> rawResponseFilters,
            @Singular final List<QueryFilter> queryFilters,
            @Singular final List<HeaderFilter> headerFilters,
            @Singular final List<BodyFilter> bodyFilters,
            @Singular final List<RequestFilter> requestFilters,
            @Singular final List<ResponseFilter> responseFilters,
            @Nullable final HttpLogFormatter formatter,
            @Nullable final HttpLogWriter writer) {

        @Nullable final RawRequestFilter rawRequestFilter = rawRequestFilters.stream()
                .reduce(RawRequestFilter::merge)
                .orElse(null);

        @Nullable final RawResponseFilter rawResponseFilter = rawResponseFilters.stream()
                .reduce(RawResponseFilter::merge)
                .orElse(null);

        @Nullable final QueryFilter queryFilter = queryFilters.stream()
                .reduce(QueryFilter::merge)
                .orElse(null);

        @Nullable final HeaderFilter headerFilter = headerFilters.stream()
                .reduce(HeaderFilter::merge)
                .orElse(null);

        @Nullable final BodyFilter bodyFilter = bodyFilters.stream()
                .reduce(BodyFilter::merge)
                .orElse(null);

        @Nullable final RequestFilter requestFilter = requestFilters.stream()
                .reduce(RequestFilter::merge)
                .orElse(null);

        @Nullable final ResponseFilter responseFilter = responseFilters.stream()
                .reduce(ResponseFilter::merge)
                .orElse(null);

        final LogbookFactory factory = LogbookFactory.INSTANCE;

        return factory.create(
                condition,
                rawRequestFilter,
                rawResponseFilter,
                queryFilter,
                headerFilter,
                bodyFilter,
                requestFilter,
                responseFilter,
                formatter,
                writer);
    }

}
