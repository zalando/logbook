package org.zalando.logbook;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        
        autoconfigurationLogbook = ac.logbook(ac.requestCondition(), Arrays.asList(ac.headerFilter()), Arrays.asList(ac.queryFilter()), Arrays.asList(ac.bodyFilter()), Arrays.asList(ac.requestFilter()), Arrays.asList(ac.responseFilter()), ac.strategy(), ac.sink(ac.httpFormatter(), ac.writer()));

        final Sink sink = new LogstashLogbackSink(state.getJsonHttpLogFormatter());
        
        autoconfigurationLogstashLogbook = ac.logbook(ac.requestCondition(), Arrays.asList(ac.headerFilter()), Arrays.asList(ac.queryFilter()), Arrays.asList(ac.bodyFilter(), new CompactingJsonBodyFilter()), Arrays.asList(ac.requestFilter()), Arrays.asList(ac.responseFilter()), ac.strategy(), sink);

        final Sink noop = new LogstashLogbackSink(state.getNoopHttpLogFormatter());

        noopHttpLogFormatterLogbook = ac.logbook(ac.requestCondition(), Arrays.asList(ac.headerFilter()), Arrays.asList(ac.queryFilter()), Arrays.asList(ac.bodyFilter(), new CompactingJsonBodyFilter()), Arrays.asList(ac.requestFilter()), Arrays.asList(ac.responseFilter()), ac.strategy(), noop);
    }

    public Map<String, List<String>> defaultRequestHeaders() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("Accept-Encoding", Arrays.asList("gzip,deflate"));
        map.put("Accept", Arrays.asList("application/json"));
        map.put("User-Agent", Arrays.asList("Apache-HttpClient/x.y.z (Java/1.8.0_xxx)"));
        map.put("Host", Arrays.asList("https://github.com"));
        map.put("Content-Length", Arrays.asList("256"));
        return map;
    }

    public Map<String, List<String>> defaultSpringSecurityResponseHeaders() {
        // https://docs.spring.io/spring-security/site/docs/5.0.x/reference/html/headers.html
        Map<String, List<String>> map = new HashMap<>();
        map.put("Cache-Control", Arrays.asList("no-cache, no-store, max-age=0, must-revalidate"));
        map.put("Pragma", Arrays.asList("no-cache"));
        map.put("Expires", Arrays.asList("0"));
        map.put("X-Content-Type-Options", Arrays.asList("nosniff"));
        map.put("Strict-Transport-Security", Arrays.asList("max-age=31536000 ; includeSubDomains"));
        map.put("X-Frame-Options", Arrays.asList("DENY"));
        map.put("X-XSS-Protection", Arrays.asList("1; mode=block"));
        return map;
    }

    public Map<String, List<String>> defaultIstioHeaders() {
        Map<String, List<String>> map = new HashMap<>();
        
        return defaultIstioHeaders(map);
    }

    public Map<String, List<String>> defaultOpenIdConnectHeaders() {
        Map<String, List<String>> map = new HashMap<>();
        
        return defaultIstioHeaders(map);
    }

    public Map<String, List<String>> defaultOpenIdConnectHeaders(Map<String, List<String>> map) {
        map.put("Authorization", Arrays.asList(UUID.randomUUID().toString()));
        return map;
    }

    public Map<String, List<String>> defaultIstioHeaders(Map<String, List<String>> map) {
        // HTTP header names
        map.put("x-request-id", Arrays.asList(UUID.randomUUID().toString()));
        map.put("x-b3-traceid", Arrays.asList(UUID.randomUUID().toString()));
        map.put("x-b3-spanid", Arrays.asList(UUID.randomUUID().toString()));
        map.put("x-b3-parentspanid", Arrays.asList("1"));
        map.put("x-b3-flags", Arrays.asList(UUID.randomUUID().toString()));
        map.put("x-ot-span-context", Arrays.asList(UUID.randomUUID().toString()));
        return map;
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
