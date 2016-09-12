package org.zalando.logbook;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public final class HeaderFilterTest {

    @Test
    public void noneShouldDefaultToNoOp() {
        final HeaderFilter unit = HeaderFilter.none();

        final Map<String, List<String>> headers = singletonMap("Authorization", singletonList("Bearer s3cr3t"));
        assertThat(unit.filter(headers), is(sameInstance(headers)));
    }

}
