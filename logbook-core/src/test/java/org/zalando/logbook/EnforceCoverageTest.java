package org.zalando.logbook;

import com.google.gag.annotation.remark.Hack;
import com.google.gag.annotation.remark.OhNoYouDidnt;
import org.junit.Test;

@Hack
@OhNoYouDidnt
public final class EnforceCoverageTest {

    @Test
    public void shouldUseConditionsConstructor() {
        new Conditions();
    }

    @Test
    public void shouldUseGlobConstructor() {
        new Glob();
    }

    @Test
    public void shouldUseFiltersConstructor() {
        new Filters();
    }
}
