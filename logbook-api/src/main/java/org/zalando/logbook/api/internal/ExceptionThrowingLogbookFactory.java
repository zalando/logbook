package org.zalando.logbook.api.internal;

import org.zalando.logbook.api.BodyFilter;
import org.zalando.logbook.api.CorrelationId;
import org.zalando.logbook.api.HeaderFilter;
import org.zalando.logbook.api.HttpRequest;
import org.zalando.logbook.api.Logbook;
import org.zalando.logbook.api.LogbookFactory;
import org.zalando.logbook.api.PathFilter;
import org.zalando.logbook.api.QueryFilter;
import org.zalando.logbook.api.RequestFilter;
import org.zalando.logbook.api.ResponseFilter;
import org.zalando.logbook.api.Sink;
import org.zalando.logbook.api.Strategy;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * <p>
 * This class is a default implementation of <code>LogbookFactory</code>.
 * It is loaded by default throw Java <code>ServiceLoader</code> mechanism
 * if there are no other implementation of <code>LogbookFactory</code>
 * found on the classpath. This is because the priority of this class is set to
 * <code>Integer.MIN_VALUE</code>.
 * </p>
 * <p>
 * This factory creates an instance of <code>ExceptionThrowingLogbook</code>,
 * whose methods throw <code>UnsupportedOperationException</code>.
 * </p>
 */
public final class ExceptionThrowingLogbookFactory implements LogbookFactory {

    @Override
    public int getPriority() {
        return Integer.MIN_VALUE;
    }

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

        return new ExceptionThrowingLogbook(
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
