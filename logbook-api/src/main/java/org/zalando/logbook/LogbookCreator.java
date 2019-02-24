package org.zalando.logbook;

import lombok.Singular;
import org.apiguardian.api.API;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.apiguardian.api.API.Status.STABLE;


@API(status = INTERNAL)
public final class LogbookCreator {

    private LogbookCreator() {

    }

    @API(status = STABLE)
    public static final class Builder {

    }

    @lombok.Builder(builderClassName = "Builder")
    private static Logbook create(
            @Nullable final Predicate<HttpRequest> condition,
            @Singular final List<QueryFilter> queryFilters,
            @Singular final List<HeaderFilter> headerFilters,
            @Singular final List<BodyFilter> bodyFilters,
            @Singular final List<RequestFilter> requestFilters,
            @Singular final List<ResponseFilter> responseFilters,
            @Nullable final Strategy strategy,
            @Nullable final Sink sink) {

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
                queryFilter,
                headerFilter,
                bodyFilter,
                requestFilter,
                responseFilter,
                strategy,
                sink);
    }

}
