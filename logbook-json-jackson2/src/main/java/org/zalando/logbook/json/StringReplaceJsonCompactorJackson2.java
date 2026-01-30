package org.zalando.logbook.json;

final class StringReplaceJsonCompactorJackson2 implements JsonCompactorJackson2 {
    @Override
    public String compact(final String json) {
        return json.replace("\n", "");
    }
}
