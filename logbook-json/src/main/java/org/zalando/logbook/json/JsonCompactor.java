package org.zalando.logbook.json;

import java.io.IOException;

interface JsonCompactor {

    String compact(String json) throws IOException;

}
