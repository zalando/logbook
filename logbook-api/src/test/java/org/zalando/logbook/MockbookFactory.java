package org.zalando.logbook;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public final class MockbookFactory implements LogbookFactory {

    @Override
    public Logbook create(
            @Nullable final Predicate<RawHttpRequest> condition,
            @Nullable final RawRequestFilter rawRequestFilter,
            @Nullable final RawResponseFilter rawResponseFilter,
            @Nullable final QueryFilter queryFilter,
            @Nullable final HeaderFilter headerFilter,
            @Nullable final BodyFilter bodyFilter,
            @Nullable final RequestFilter requestFilter,
            @Nullable final ResponseFilter responseFilter,
            @Nullable final HttpLogFormatter formatter,
            @Nullable final HttpLogWriter writer) {

        return new Mockbook(
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
