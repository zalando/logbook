package org.zalando.logbook.benchmark;

import lombok.Getter;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Sink;
import org.zalando.logbook.autoconfigure.LogbookAutoConfiguration;
import org.zalando.logbook.autoconfigure.LogbookProperties;
import org.zalando.logbook.json.CompactingJsonBodyFilter;
import org.zalando.logbook.logstash.LogstashLogbackSink;

import java.util.List;
import java.util.Collections;

@Getter
@State(Scope.Benchmark)
public class LogbookState {

    private Logbook autoconfigurationLogbook;
    private Logbook autoconfigurationLogstashLogbook;
    private Logbook noopHttpLogFormatterLogbook;

    @Setup(Level.Trial)
    public void setUp(final HttpLogFormatterState state) {
        final LogbookProperties properties = new LogbookProperties();
        final LogbookAutoConfiguration ac = new LogbookAutoConfiguration(properties);

        autoconfigurationLogbook = ac.logbook(ac.requestCondition(), ac.correlationId(), Collections.singletonList(ac.headerFilter()), Collections.singletonList(ac.pathFilter()), Collections.singletonList(ac.queryFilter()), Collections.singletonList(ac.bodyFilter()), Collections.singletonList(ac.requestFilter()), Collections.singletonList(ac.responseFilter()), ac.strategy(), null, ac.sink(ac.httpFormatter(), ac.writer()));

        final Sink sink = new LogstashLogbackSink(state.getJsonHttpLogFormatter());

        autoconfigurationLogstashLogbook = ac.logbook(ac.requestCondition(), ac.correlationId(), Collections.singletonList(ac.headerFilter()), Collections.singletonList(ac.pathFilter()), Collections.singletonList(ac.queryFilter()), List.of(ac.bodyFilter(), new CompactingJsonBodyFilter()), Collections.singletonList(ac.requestFilter()), Collections.singletonList(ac.responseFilter()), ac.strategy(), null, sink);

        final Sink noop = new LogstashLogbackSink(state.getNoopHttpLogFormatter());

        noopHttpLogFormatterLogbook = ac.logbook(ac.requestCondition(), ac.correlationId(), Collections.singletonList(ac.headerFilter()), Collections.singletonList(ac.pathFilter()), Collections.singletonList(ac.queryFilter()), List.of(ac.bodyFilter(), new CompactingJsonBodyFilter()), Collections.singletonList(ac.requestFilter()), Collections.singletonList(ac.responseFilter()), ac.strategy(), null, noop);
    }


}
