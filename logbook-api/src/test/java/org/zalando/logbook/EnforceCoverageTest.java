package org.zalando.logbook;

import com.google.gag.annotation.remark.Hack;
import com.google.gag.annotation.remark.OhNoYouDidnt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@Hack
@OhNoYouDidnt
public final class EnforceCoverageTest {

    @Test
    void shouldCoverUselessClearMethods() {
        final LogbookCreator.Builder builder = Logbook.builder();

        builder.clearRawRequestFilters();
        builder.clearRawResponseFilters();
        builder.clearQueryFilters();
        builder.clearHeaderFilters();
        builder.clearBodyFilters();
        builder.clearRequestFilters();
        builder.clearResponseFilters();

        builder.rawRequestFilter(mock(RawRequestFilter.class));
        builder.rawResponseFilter(mock(RawResponseFilter.class));
        builder.queryFilter(mock(QueryFilter.class));
        builder.headerFilter(mock(HeaderFilter.class));
        builder.bodyFilter(mock(BodyFilter.class));
        builder.requestFilter(mock(RequestFilter.class));
        builder.responseFilter(mock(ResponseFilter.class));

        builder.clearRawRequestFilters();
        builder.clearRawResponseFilters();
        builder.clearQueryFilters();
        builder.clearHeaderFilters();
        builder.clearBodyFilters();
        builder.clearRequestFilters();
        builder.clearResponseFilters();
    }

    @Test
    void fakeLogbookShouldThrow() {
        assertThrows(UnsupportedOperationException.class, () ->
                Logbook.create().write(mock(RawHttpRequest.class)));
    }
}
