package org.zalando.logbook.benchmark;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.core.DefaultHttpLogFormatter;
import org.zalando.logbook.json.FastJsonHttpLogFormatter;
import org.zalando.logbook.json.JsonHttpLogFormatterJackson2;

@State(Scope.Benchmark)
public class HttpLogFormatterState {

    private NoopHttpLogFormatter noopHttpLogFormatter = new NoopHttpLogFormatter();

    private JsonHttpLogFormatterJackson2 jsonHttpLogFormatterJackson2 = new JsonHttpLogFormatterJackson2();
    private FastJsonHttpLogFormatter fastJsonHttpLogFormatter = new FastJsonHttpLogFormatter();
    private HttpLogFormatter defaultHttpLogFormatter = new DefaultHttpLogFormatter();

    public JsonHttpLogFormatterJackson2 getJsonHttpLogFormatter() {
        return jsonHttpLogFormatterJackson2;
    }

    public FastJsonHttpLogFormatter getFastJsonHttpLogFormatter() {
        return fastJsonHttpLogFormatter;
    }

    public HttpLogFormatter getDefaultHttpLogFormatter() {
        return defaultHttpLogFormatter;
    }

    public NoopHttpLogFormatter getNoopHttpLogFormatter() {
        return noopHttpLogFormatter;
    }
}
