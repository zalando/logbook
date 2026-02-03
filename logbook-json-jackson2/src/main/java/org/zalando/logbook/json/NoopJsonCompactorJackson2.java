package org.zalando.logbook.json;

import lombok.Generated;

@Deprecated(since = "4.0.0", forRemoval = true)
@Generated
final class NoopJsonCompactorJackson2 implements JsonCompactorJackson2 {

    @Override
    public String compact(final String json) {
        return json;
    }
}
