package org.zalando.logbook;

import java.io.IOException;
import java.util.Map;

public interface StructuredHttpLogFormatterSupport {

    Map<String, Object> resolveMembers(Precorrelation precorrelation, HttpRequest httpRequest) throws IOException;

    Map<String, Object> resolveMembers(Correlation correlation, HttpResponse httpResponse) throws IOException;

    String format(Map<String, Object> members);
}
