package org.zalando.logbook.api.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.zalando.logbook.api.BodyFilter;
import org.zalando.logbook.api.CorrelationId;
import org.zalando.logbook.api.HeaderFilter;
import org.zalando.logbook.api.HttpRequest;
import org.zalando.logbook.api.Logbook;
import org.zalando.logbook.api.PathFilter;
import org.zalando.logbook.api.QueryFilter;
import org.zalando.logbook.api.RequestFilter;
import org.zalando.logbook.api.ResponseFilter;
import org.zalando.logbook.api.Sink;
import org.zalando.logbook.api.Strategy;

import java.util.function.Predicate;

/**
 * This class is a default implementation of <code>Logbook</code>.
 * It throws <code>UnsupportedOperationException</code>
 * if any of its methods are called.
 *
 * @see ExceptionThrowingLogbookFactory
 */
@AllArgsConstructor
@Getter
final class ExceptionThrowingLogbook implements Logbook {

    private final Predicate<HttpRequest> predicate;
    private final CorrelationId correlationId;
    private final QueryFilter queryFilter;
    private final PathFilter pathFilter;
    private final HeaderFilter headerFilter;
    private final BodyFilter bodyFilter;
    private final RequestFilter requestFilter;
    private final ResponseFilter responseFilter;
    private final Strategy strategy;
    private final Sink sink;

    @Override
    public RequestWritingStage process(final HttpRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RequestWritingStage process(final HttpRequest request, final Strategy strategy) {
        throw new UnsupportedOperationException();
    }

}
