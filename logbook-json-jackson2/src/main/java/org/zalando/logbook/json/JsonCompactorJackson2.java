package org.zalando.logbook.json;

import java.io.IOException;

interface JsonCompactorJackson2 {
    String compact(String json) throws IOException;
}
