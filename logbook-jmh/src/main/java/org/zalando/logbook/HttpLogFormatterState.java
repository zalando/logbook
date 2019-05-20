package org.zalando.logbook;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.zalando.logbook.json.JsonHttpLogFormatter;

@State(Scope.Benchmark)
public class HttpLogFormatterState {

    private NoopHttpLogFormatter noopHttpLogFormatter = new NoopHttpLogFormatter();
    
    private JsonHttpLogFormatter jsonHttpLogFormatter = new JsonHttpLogFormatter();
    private HttpLogFormatter defaultHttpLogFormatter = new DefaultHttpLogFormatter();
    
    public JsonHttpLogFormatter getJsonHttpLogFormatter() {
        return jsonHttpLogFormatter;
    }

    public HttpLogFormatter getDefaultHttpLogFormatter() {
        return defaultHttpLogFormatter;
    }

    public NoopHttpLogFormatter getNoopHttpLogFormatter() {
        return noopHttpLogFormatter;
    }
}
