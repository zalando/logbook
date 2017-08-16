package org.zalando.logbook;

import com.google.gag.annotation.remark.Hack;
import com.google.gag.annotation.remark.OhNoYouDidnt;
import org.junit.jupiter.api.Test;

@Hack
@OhNoYouDidnt
public final class EnforceCoverageTest {

    @Test
    void shouldUseBodyFiltersConstructor() {
        new BodyFilters();
    }

    @Test
    void shouldUseBodyReplacersConstructor() {
        new BodyReplacers();
    }

    @Test
    void shouldUseConditionsConstructor() {
        new Conditions();
    }

    @Test
    void shouldUseGlobConstructor() {
        new Glob();
    }

    @Test
    void shouldUseHeaderFiltersConstructor() {
        new HeaderFilters();
    }

    @Test
    void shouldUseMediaTypeQueryConstructor() {
        new MediaTypeQuery();
    }

    @Test
    void shouldUsePatternLikeConstructor() {
        new PatternLike();
    }

    @Test
    void shouldUseQueryFiltersConstructor() {
        new QueryFilters();
    }

    @Test
    void shouldUseRawRequestFiltersConstructor() {
        new RawRequestFilters();
    }

    @Test
    void shouldUseRawResponseFiltersConstructor() {
        new RawResponseFilters();
    }

    @Test
    void shouldUseReplacersConstructor() {
        new BodyReplacers();
    }

}
