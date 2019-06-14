package org.zalando.logbook.json;

final class StringReplaceJsonCompactor implements JsonCompactor {

    @Override
    public String compact(final String json) {
        return json.replace("\n", "");
    }

}
