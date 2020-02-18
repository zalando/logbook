package org.zalando.logbook;

import org.apiguardian.api.API;

import javax.annotation.Nullable;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
@FunctionalInterface
public interface BodyFilter {

    String filter(@Nullable final String contentType, final String body);

    @Nullable
    default BodyFilter tryMerge(final BodyFilter next) {
        return null;
    }
    
    static BodyFilter none() {
        return NoneBodyFilter.NONE;
    }

    static BodyFilter merge(final BodyFilter left, final BodyFilter right) {
        @Nullable final BodyFilter merged = left.tryMerge(right);

        if (merged == null) {
            return new NonMergeableBodyFilterPair(left, right);
        } else {
            return merged;
        }
    }

}
