package org.zalando.logbook.json;

@Deprecated(since = "4.0.0", forRemoval = true)
final class StringReplaceJsonCompactorJackson2 implements JsonCompactorJackson2 {
    @Override
    public String compact(final String json) {
        return json.replace("\n", "");
    }
}
