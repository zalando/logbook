package org.zalando.logbook.benchmark;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.core.DefaultHttpLogFormatter;
import org.zalando.logbook.json.FastJsonHttpLogFormatterJackson2;
import org.zalando.logbook.json.JsonHttpLogFormatter;

@State(Scope.Benchmark)
public class HttpLogFormatterState {

    private NoopHttpLogFormatter noopHttpLogFormatter = new NoopHttpLogFormatter();

    private JsonHttpLogFormatter jsonHttpLogFormatter = new JsonHttpLogFormatter();
    private FastJsonHttpLogFormatterJackson2 fastJsonHttpLogFormatterJackson2 = new FastJsonHttpLogFormatterJackson2();
    private HttpLogFormatter defaultHttpLogFormatter = new DefaultHttpLogFormatter();

    public JsonHttpLogFormatter getJsonHttpLogFormatter() {
        return jsonHttpLogFormatter;
    }

    public FastJsonHttpLogFormatterJackson2 getFastJsonHttpLogFormatter() {
        return fastJsonHttpLogFormatterJackson2;
    }

    public HttpLogFormatter getDefaultHttpLogFormatter() {
        return defaultHttpLogFormatter;
    }

    public NoopHttpLogFormatter getNoopHttpLogFormatter() {
        return noopHttpLogFormatter;
    }
}
