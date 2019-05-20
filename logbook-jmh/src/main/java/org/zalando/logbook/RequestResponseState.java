package org.zalando.logbook;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.zalando.logbook.jmh.DefaultCorrelation;
import org.zalando.logbook.jmh.DefaultPrecorrelation;

@State(Scope.Benchmark)
public class RequestResponseState {

    protected HttpResponse response;
    protected HttpRequest request;
    
    protected DefaultCorrelation defaultCorrelation = new DefaultCorrelation("id", Duration.ofMillis(100));
    protected DefaultPrecorrelation defaultPrecorrelation = new DefaultPrecorrelation("id", defaultCorrelation);

    protected HttpResponse minimalResponse;
    protected Correlation correlation;
    
    protected HttpRequest minimalRequest;
    protected Precorrelation precorrelation; 
    
    @Setup(Level.Trial)
    public void setUp() throws Exception {
        minimalResponse = MockHttpResponse.create()
                .withContentType("application/json")
                .withBodyAsString("{\"name\":\"Bob\"}");
        
        minimalRequest = MockHttpRequest.create()
                .withContentType("application/json")
                .withBodyAsString("{\"name\":\"Bob\"}");
        
        request = MockHttpRequest.create()
                .withContentType("application/json")
                .withHeaders(defaultOpenIdConnectHeaders(defaultIstioHeaders(defaultRequestHeaders())))
                .withBodyAsString("{\"name\":\"Bob\"}");

        response = MockHttpResponse.create()
                .withContentType("application/json")
                .withHeaders(defaultIstioHeaders(defaultSpringSecurityResponseHeaders()))
                .withBodyAsString("{\"name\":\"Bob\"}");
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
        
    public HttpRequest getRequest() {
        return request;
    }
    
    public HttpResponse getResponse() {
        return response;
    }
    
    public DefaultCorrelation getDefaultCorrelation() {
        return defaultCorrelation;
    }
    public DefaultPrecorrelation getDefaultPrecorrelation() {
        return defaultPrecorrelation;
    }
    
    public HttpRequest getMinimalRequest() {
        return minimalRequest;
    }
    
    public HttpResponse getMinimalResponse() {
        return minimalResponse;
    }
    
    public Correlation getCorrelation() {
        return correlation;
    }
    
    public Precorrelation getPrecorrelation() {
        return precorrelation;
    }
}
