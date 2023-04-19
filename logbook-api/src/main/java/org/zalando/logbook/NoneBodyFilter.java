package org.zalando.logbook;

import javax.annotation.Nullable;

enum NoneBodyFilter implements BodyFilter {

    NONE;

    @Override
    public String filter(
            @Nullable final String contentType, final String body) {
        return body;
    }

    @Override
    public BodyFilter tryMerge(final BodyFilter next) {
        return next;
    }

}
