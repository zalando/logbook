package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

public final class HeaderFilterTest {

    @Test
    void noneShouldDefaultToNoOp() {
        final HeaderFilter unit = HeaderFilter.none();

        final Map<String, List<String>> headers = singletonMap("Authorization", singletonList("Bearer s3cr3t"));
        assertThat(unit.filter(headers), is(sameInstance(headers)));
    }

}
