package org.zalando.logbook.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nullable;

import static lombok.AccessLevel.PACKAGE;
import static org.zalando.logbook.api.BodyFilter.merge;

@AllArgsConstructor
@Getter(PACKAGE)
final class NonMergeableBodyFilterPair implements BodyFilter {

    private final BodyFilter left;
    private final BodyFilter right;

    @Override
    public String filter(
            @Nullable final String contentType, final String body) {
        return right.filter(contentType, left.filter(contentType, body));
    }

    @Nullable
    @Override
    public BodyFilter tryMerge(final BodyFilter next) {
        @Nullable final BodyFilter filter = right.tryMerge(next);

        if (filter == null) {
            return null;
        }

        return merge(left, filter);
    }

}
