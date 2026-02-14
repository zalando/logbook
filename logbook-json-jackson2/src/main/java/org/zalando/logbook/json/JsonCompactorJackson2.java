package org.zalando.logbook.json;

import java.io.IOException;

@Deprecated(since = "4.0.0", forRemoval = true)
interface JsonCompactorJackson2 {
    String compact(String json) throws IOException;
}
