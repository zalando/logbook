package org.zalando.logbook.api;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;

class BodyFilterMergeTest {

    private static final class IdentityFilter implements BodyFilter {

        @Override
        public String filter(
                @Nullable final String contentType, final String body) {
            return body;
        }

    }

    @AllArgsConstructor
    private static final class TestFilter implements BodyFilter {

        private final List<String> names;

        public TestFilter(final String s) {
            this(singletonList(s));
        }

        @Override
        public String filter(
                @Nullable final String contentType, final String body) {

            return body;
        }

        @Nullable
        @Override
        public BodyFilter tryMerge(final BodyFilter next) {
            if (next instanceof TestFilter) {
                final TestFilter filter = (TestFilter) next;
                return new TestFilter(concat(names, filter.names));
            }

            return null;
        }

        @SafeVarargs
        private static <T> List<T> concat(final List<T>... lists) {
            return Arrays.stream(lists)
                    .flatMap(Collection::stream)
                    .collect(toList());
        }

    }

    @Test
    void noneMergesWithAnything() {
        final BodyFilter unit = BodyFilter.none();
        final BodyFilter other = (contentType, body) -> body;

        final BodyFilter actual = unit.tryMerge(other);

        assertSame(other, actual);
    }

    @Test
    void mergesConsecutiveMergeableFilters() {
        final BodyFilter actual = merge(
                new TestFilter("a"),
                new TestFilter("b"),
                new TestFilter("c"));

        assertThat(actual).isInstanceOf(TestFilter.class);

        final TestFilter result = (TestFilter) actual;
        assertThat(result.names).contains("a", "b", "c");
    }

    @Test
    void nonMergeablePairMergesRightSide() {
        final BodyFilter pair = BodyFilter.merge(
                new IdentityFilter(),
                new TestFilter("a"));

        final BodyFilter actual = merge(pair, new TestFilter("b"));

        assertThat(actual).isInstanceOf(NonMergeableBodyFilterPair.class);

        final NonMergeableBodyFilterPair result =
                (NonMergeableBodyFilterPair) actual;

        assertThat(result.getLeft()).isInstanceOf(IdentityFilter.class);
        assertThat(result.getRight()).isInstanceOf(TestFilter.class);

        final TestFilter right = (TestFilter) result.getRight();
        assertThat(right.names).contains("a", "b");
    }

    private static BodyFilter merge(final BodyFilter... filters) {
        return Arrays.stream(filters)
                .reduce(BodyFilter::merge)
                .orElseThrow(AssertionError::new);
    }

}
