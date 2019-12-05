package org.zalando.logbook;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public final class MockbookFactory implements LogbookFactory {

    @Override
    public Logbook create(
            @Nullable final Predicate<HttpRequest> condition,
            @Nullable final CorrelationId correlationId,
            @Nullable final QueryFilter queryFilter,
            @Nullable final PathFilter pathFilter,
            @Nullable final HeaderFilter headerFilter,
            @Nullable final BodyFilter bodyFilter,
            @Nullable final RequestFilter requestFilter,
            @Nullable final ResponseFilter responseFilter,
            @Nullable final Strategy strategy,
            @Nullable final Sink sink) {

        return new Mockbook(
                condition,
                correlationId,
                queryFilter,
                pathFilter,
                headerFilter,
                bodyFilter,
                requestFilter,
                responseFilter,
                strategy,
                sink);
    }

}
