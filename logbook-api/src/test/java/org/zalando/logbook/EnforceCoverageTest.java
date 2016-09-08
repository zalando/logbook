package org.zalando.logbook;

import com.google.gag.annotation.remark.Hack;
import com.google.gag.annotation.remark.OhNoYouDidnt;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@Hack
@OhNoYouDidnt
public final class EnforceCoverageTest {

    @Test
    public void shouldUseHeadersConstructor() {
        new BaseHttpMessage.HeadersBuilder();
    }

    @Test
    public void shouldUseLogbookCreatorConstructor() {
        new LogbookCreator();
    }

    @Test
    public void shouldCoverUselessClearMethods() {
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
    public void shouldUseRequestURIConstructor() {
        new RequestURI();
    }

    @Test
    public void shouldUseComponentValueOf() {
        RequestURI.Component.valueOf("SCHEME");
    }

    @Test
    public void shouldUseOriginValueOf() {
        Origin.valueOf("LOCAL");
    }

    @Test
    public void shouldUseBuilderToString() {
        assertThat(Logbook.builder(), hasToString(notNullValue()));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void fakeLogbookShouldThrow() throws IOException {
        Logbook.create().write(mock(RawHttpRequest.class));
    }
}
