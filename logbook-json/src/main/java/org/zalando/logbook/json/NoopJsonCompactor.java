package org.zalando.logbook.json;

final class NoopJsonCompactor implements JsonCompactor {

    @Override
    public String compact(final String json) {
        return json;
    }
}
