package org.zalando.logbook;

import com.google.gag.annotation.remark.Hack;
import com.google.gag.annotation.remark.OhNoYouDidnt;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@Hack
@OhNoYouDidnt
public final class EnforceCoverageTest {

    @Test
    void shouldUseHeadersConstructor() {
        new BaseHttpMessage.HeadersBuilder();
    }

    @Test
    void shouldUseLogbookCreatorConstructor() {
        new LogbookCreator();
    }

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
    void shouldUseRequestURIConstructor() {
        new RequestURI();
    }

    @Test
    void shouldUseComponentValueOf() {
        RequestURI.Component.valueOf("SCHEME");
    }

    @Test
    void shouldUseOriginValueOf() {
        Origin.valueOf("LOCAL");
    }

    @Test
    void shouldUseBuilderToString() {
        assertThat(Logbook.builder(), hasToString(notNullValue()));
    }

    @Test
    void fakeLogbookShouldThrow() throws IOException {
        assertThrows(UnsupportedOperationException.class, () ->
                Logbook.create().write(mock(RawHttpRequest.class)));
    }
}
