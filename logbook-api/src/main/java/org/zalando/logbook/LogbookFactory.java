package org.zalando.logbook;

import org.apiguardian.api.API;

import javax.annotation.Nullable;

import java.util.function.Predicate;

import static java.util.ServiceLoader.load;
import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
interface LogbookFactory {

    LogbookFactory INSTANCE = load(LogbookFactory.class).iterator().next();

    Logbook create(
            @Nullable final Predicate<HttpRequest> condition,
            @Nullable final QueryFilter queryFilter,
            @Nullable final HeaderFilter headerFilter,
            @Nullable final BodyFilter bodyFilter,
            @Nullable final RequestFilter requestFilter,
            @Nullable final ResponseFilter responseFilter,
            @Nullable final Strategy strategy,
            @Nullable final Sink sink);

}
