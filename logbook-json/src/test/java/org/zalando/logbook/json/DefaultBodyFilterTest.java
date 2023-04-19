package org.zalando.logbook.json;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.api.BodyFilter;

import static java.util.ServiceLoader.load;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;
import static org.assertj.core.util.Lists.newArrayList;

final class DefaultBodyFilterTest {

    @Test
    void shouldDeclareCompactingJsonBodyFilterByDefault() {
        assertThat(newArrayList(load(BodyFilter.class)))
                .satisfies(filter ->
                        assertThat(filter).isInstanceOf(CompactingJsonBodyFilter.class), atIndex(0))
                .satisfies(filter ->
                        assertThat(filter).isInstanceOf(AccessTokenBodyFilter.class), atIndex(1));
    }

}
