package org.zalando.logbook.api;

import com.google.gag.annotation.remark.Hack;
import com.google.gag.annotation.remark.OhNoYouDidnt;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.api.internal.ExceptionThrowingLogbookFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@Hack
@OhNoYouDidnt
final class EnforceCoverageTest {

    @Test
    void shouldCoverUselessClearMethods() {
        final LogbookCreator.Builder builder = Logbook.builder();

        builder.clearQueryFilters();
        builder.clearHeaderFilters();
        builder.clearPathFilters();
        builder.clearBodyFilters();
        builder.clearRequestFilters();
        builder.clearResponseFilters();

        builder.queryFilter(mock(QueryFilter.class));
        builder.headerFilter(mock(HeaderFilter.class));
        builder.pathFilter(mock(PathFilter.class));
        builder.bodyFilter(mock(BodyFilter.class));
        builder.requestFilter(mock(RequestFilter.class));
        builder.responseFilter(mock(ResponseFilter.class));

        builder.clearQueryFilters();
        builder.clearHeaderFilters();
        builder.clearPathFilters();
        builder.clearBodyFilters();
        builder.clearRequestFilters();
        builder.clearResponseFilters();
    }

    @Test
    void fakeLogbookShouldThrow() {
        Logbook logbook = Logbook.create();

        assertThrows(UnsupportedOperationException.class, () ->
                logbook.process(mock(HttpRequest.class)));
        assertThrows(UnsupportedOperationException.class, () ->
                logbook.process(mock(HttpRequest.class), mock(Strategy.class)));
    }

    @Test
    void shouldHaveCorrectPriority() {
        assertEquals(Integer.MIN_VALUE, ExceptionThrowingLogbookFactory.INSTANCE.getPriority());

        // Create a LogbookFactory with default getPriority()
        LogbookFactory logbookFactory = (condition, correlationId, queryFilter, pathFilter,
                                         headerFilter, bodyFilter, requestFilter, responseFilter, strategy, sink) -> null;
        assertEquals(0, logbookFactory.getPriority());
    }
}
