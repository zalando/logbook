package org.zalando.logbook.json;

import org.zalando.logbook.common.MediaTypeQuery;

import java.util.function.Predicate;

final class JsonMediaType {

    private JsonMediaType() {

    }

    static final Predicate<String> JSON = MediaTypeQuery.compile("application/json", "application/*+json");

}
