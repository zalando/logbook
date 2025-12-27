package org.zalando.logbook.internal;

import org.zalando.logbook.BodyFilter;
import org.zalando.logbook.CorrelationId;
import org.zalando.logbook.HeaderFilter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.LogbookFactory;
import org.zalando.logbook.PathFilter;
import org.zalando.logbook.QueryFilter;
import org.zalando.logbook.RequestFilter;
import org.zalando.logbook.ResponseFilter;
import org.zalando.logbook.Sink;
import org.zalando.logbook.Strategy;
import org.zalando.logbook.attributes.AttributeExtractor;

import jakarta.annotation.Nullable;
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
            @Nullable final AttributeExtractor attributeExtractor,
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
                attributeExtractor,
                sink);
    }

}
