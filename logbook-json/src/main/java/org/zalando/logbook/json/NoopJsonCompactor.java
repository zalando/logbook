package org.zalando.logbook.json;

import lombok.Generated;

@Generated
final class NoopJsonCompactor implements JsonCompactor {

    @Override
    public String compact(final String json) {
        return json;
    }
}
