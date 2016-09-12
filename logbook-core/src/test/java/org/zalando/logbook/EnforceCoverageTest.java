package org.zalando.logbook;

import com.google.gag.annotation.remark.Hack;
import com.google.gag.annotation.remark.OhNoYouDidnt;
import org.junit.Test;

@Hack
@OhNoYouDidnt
public final class EnforceCoverageTest {

    @Test
    public void shouldUseBodyFiltersConstructor() {
        new BodyFilters();
    }

    @Test
    public void shouldUseBodyReplacersConstructor() {
        new BodyReplacers();
    }

    @Test
    public void shouldUseConditionsConstructor() {
        new Conditions();
    }

    @Test
    public void shouldUseGlobConstructor() {
        new Glob();
    }

    @Test
    public void shouldUseHeaderFiltersConstructor() {
        new HeaderFilters();
    }

    @Test
    public void shouldUseMediaTypeQueryConstructor() {
        new MediaTypeQuery();
    }

    @Test
    public void shouldUsePatternLikeConstructor() {
        new PatternLike();
    }

    @Test
    public void shouldUseQueryFiltersConstructor() {
        new QueryFilters();
    }

    @Test
    public void shouldUseRawRequestFiltersConstructor() {
        new RawRequestFilters();
    }

    @Test
    public void shouldUseRawResponseFiltersConstructor() {
        new RawResponseFilters();
    }

    @Test
    public void shouldUseReplacersConstructor() {
        new BodyReplacers();
    }

}
