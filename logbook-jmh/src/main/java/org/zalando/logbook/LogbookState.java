package org.zalando.logbook;

import java.util.Arrays;

import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.zalando.logbook.autoconfigure.LogbookAutoConfiguration;
import org.zalando.logbook.autoconfigure.LogbookProperties;
import org.zalando.logbook.json.CompactingJsonBodyFilter;
import org.zalando.logbook.logstash.LogstashLogbackSink;

@State(Scope.Benchmark)
public class LogbookState {

    private Logbook autoconfigurationLogbook;
    private Logbook autoconfigurationLogstashLogbook;
    private Logbook noopHttpLogFormatterLogbook;

    @Setup(Level.Trial)
    public void setUp(HttpLogFormatterState state) throws Exception {
        LogbookProperties properties = new LogbookProperties();
        LogbookAutoConfiguration ac = new LogbookAutoConfiguration(properties);
        
        autoconfigurationLogbook = ac.logbook(ac.requestCondition(), Arrays.asList(ac.headerFilter()), Arrays.asList(ac.pathFilter()), Arrays.asList(ac.queryFilter()), Arrays.asList(ac.bodyFilter()), Arrays.asList(ac.requestFilter()), Arrays.asList(ac.responseFilter()), ac.strategy(), ac.sink(ac.httpFormatter(), ac.writer()));

        final Sink sink = new LogstashLogbackSink(state.getJsonHttpLogFormatter());
        
        autoconfigurationLogstashLogbook = ac.logbook(ac.requestCondition(), Arrays.asList(ac.headerFilter()), Arrays.asList(ac.pathFilter()), Arrays.asList(ac.queryFilter()), Arrays.asList(ac.bodyFilter(), new CompactingJsonBodyFilter()), Arrays.asList(ac.requestFilter()), Arrays.asList(ac.responseFilter()), ac.strategy(), sink);

        final Sink noop = new LogstashLogbackSink(state.getNoopHttpLogFormatter());

        noopHttpLogFormatterLogbook = ac.logbook(ac.requestCondition(), Arrays.asList(ac.headerFilter()), Arrays.asList(ac.pathFilter()), Arrays.asList(ac.queryFilter()), Arrays.asList(ac.bodyFilter(), new CompactingJsonBodyFilter()), Arrays.asList(ac.requestFilter()), Arrays.asList(ac.responseFilter()), ac.strategy(), noop);
    }

    public Logbook getAutoconfigurationLogbook() {
        return autoconfigurationLogbook;
    }
    
    public Logbook getAutoconfigurationLogstashLogbook() {
        return autoconfigurationLogstashLogbook;
    }

    public Logbook getNoopHttpLogFormatterLogbook() {
        return noopHttpLogFormatterLogbook;
    }
    
    
}
