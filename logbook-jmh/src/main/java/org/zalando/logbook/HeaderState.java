package org.zalando.logbook;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.zalando.logbook.autoconfigure.LogbookAutoConfiguration;
import org.zalando.logbook.autoconfigure.LogbookProperties;

@State(Scope.Benchmark)
public class HeaderState {

    private Map<String, List<String>> shopifyResponseHeaders; // real-life example

    private Map<String, List<String>> postRequestHeaders;
    private Map<String, List<String>> getRequestHeaders;

    private Map<String, List<String>> allResponseHeaders;
    private Map<String, List<String>> allRequestHeaders;

    // filters
    private HeaderFilter autoconfigurationFilter;
    private HeaderFilter replaceFilter;
    private HeaderFilter replace2xFilter;
    private HeaderFilter removeFilter;
    private HeaderFilter remove2xFilter;
    
    @Setup(Level.Trial)
    public void setUp() throws Exception {
        LogbookProperties properties = new LogbookProperties();
        LogbookAutoConfiguration ac = new LogbookAutoConfiguration(properties);
        
        autoconfigurationFilter = ac.headerFilter();
        replaceFilter = HeaderFilters.authorization();
        removeFilter = HeaderFilters.removeHeaders(name -> "Authorization".equalsIgnoreCase(name));
        
        replace2xFilter = HeaderFilter.merge(
                HeaderFilters.replaceHeaders("Authorization"::equalsIgnoreCase, "XXX"),
                HeaderFilters.replaceHeaders("Set-Cookie"::equalsIgnoreCase, "XXX")
                );

        remove2xFilter = HeaderFilter.merge(
                HeaderFilters.removeHeaders(name -> "Authorization".equalsIgnoreCase(name)),
                HeaderFilters.removeHeaders(name -> "Set-Cookie".equalsIgnoreCase(name))
                );

        // header collections
        postRequestHeaders = postRequestHeaders();
        getRequestHeaders = getRequestHeaders();
        
        Map<String, List<String>> responseHeaders = getContentResponseHeaders();
        
        Map<String, List<String>> istioHeaders = defaultIstioHeaders();
        Map<String, List<String>> securityResponseHeaders = defaultSpringSecurityResponseHeaders();
        Map<String, List<String>> defaultOpenIdConnectHeaders = defaultOpenIdConnectHeaders();
        
        allRequestHeaders = new TreeMap<>();
        allRequestHeaders.putAll(postRequestHeaders);
        allRequestHeaders.putAll(istioHeaders);
        allRequestHeaders.putAll(defaultOpenIdConnectHeaders);

        allResponseHeaders = new TreeMap<>();
        allResponseHeaders.putAll(responseHeaders);
        allResponseHeaders.putAll(istioHeaders);
        allResponseHeaders.putAll(securityResponseHeaders);
        
        shopifyResponseHeaders = defaultShopifyResponse();
    }

    protected Map<String, List<String>> postRequestHeaders() {
        Map<String, List<String>> map = new TreeMap<>();
        map.put("Accept-Encoding", Arrays.asList("gzip,deflate"));
        map.put("Accept", Arrays.asList("application/json"));
        map.put("User-Agent", Arrays.asList("Apache-HttpClient/x.y.z (Java/1.8.0_xxx)"));
        map.put("Host", Arrays.asList("https://github.com"));
        map.put("Content-Type", Arrays.asList("application/json"));
        map.put("Content-Length", Arrays.asList("256"));
        return map;
    }

    protected static Map<String, List<String>> getRequestHeaders() {
        Map<String, List<String>> map = new TreeMap<>();
        map.put("Accept-Encoding", Arrays.asList("gzip,deflate"));
        map.put("Accept", Arrays.asList("application/json"));
        map.put("User-Agent", Arrays.asList("Apache-HttpClient/x.y.z (Java/1.8.0_xxx)"));
        map.put("Host", Arrays.asList("https://github.com"));
        return map;
    }

    protected static Map<String, List<String>> getContentResponseHeaders() {
        Map<String, List<String>> map = new TreeMap<>();
        map.put("Content-Type", Arrays.asList("application/json"));
        map.put("Content-Length", Arrays.asList("256"));
        map.put("Transfer-Encoding", Arrays.asList("chunked"));
        map.put("Connection", Arrays.asList("keep-alive"));
        return map;
    }    
    
    protected static Map<String, List<String>> defaultSpringSecurityResponseHeaders() {
        // https://docs.spring.io/spring-security/site/docs/5.0.x/reference/html/headers.html
        Map<String, List<String>> map = new TreeMap<>();
        map.put("Cache-Control", Arrays.asList("no-cache, no-store, max-age=0, must-revalidate"));
        map.put("Pragma", Arrays.asList("no-cache"));
        map.put("Expires", Arrays.asList("0"));
        map.put("X-Content-Type-Options", Arrays.asList("nosniff"));
        map.put("Strict-Transport-Security", Arrays.asList("max-age=31536000 ; includeSubDomains"));
        map.put("X-Frame-Options", Arrays.asList("DENY"));
        map.put("X-XSS-Protection", Arrays.asList("1; mode=block"));
        return map;
    }

    protected Map<String, List<String>> defaultOpenIdConnectHeaders() {
        Map<String, List<String>> map = new TreeMap<>();
        map.put("Authorization", Arrays.asList(UUID.randomUUID().toString()));
        return map;
    }

    protected Map<String, List<String>> defaultIstioHeaders() {
        Map<String, List<String>> map = new TreeMap<>();
        // HTTP header names
        map.put("x-request-id", Arrays.asList(UUID.randomUUID().toString()));
        map.put("x-b3-traceid", Arrays.asList(UUID.randomUUID().toString()));
        map.put("x-b3-spanid", Arrays.asList(UUID.randomUUID().toString()));
        map.put("x-b3-parentspanid", Arrays.asList("1"));
        map.put("x-b3-flags", Arrays.asList(UUID.randomUUID().toString()));
        map.put("x-ot-span-context", Arrays.asList(UUID.randomUUID().toString()));
        return map;
    }
    
    protected Map<String, List<String>> defaultShopifyResponse() {
        Map<String, List<String>> map = new TreeMap<>();
        map.put("Content-Type", Arrays.asList("application/json; charset=utf-8"));
        map.put("Transfer-Encoding", Arrays.asList("chunked"));
        map.put("Connection", Arrays.asList("keep-alive"));
        map.put("Set-Cookie", Arrays.asList("__cfduid=db9ccc357feeef085ffff7ad8b6deee870b15ggg20390; expires=Thu, 21-May-20 10:19:50 GMT; path=/; domain=.xxx.myshopify.com; HttpOnly"));
        map.put("X-Sorting-Hat-PodId", Arrays.asList("85"));
        map.put("X-Sorting-Hat-ShopId", Arrays.asList("123456789012"));
        map.put("Vary", Arrays.asList("Accept-Encoding"));
        map.put("Referrer-Policy", Arrays.asList("origin-when-cross-origin"));
        map.put("X-Frame-Options", Arrays.asList("DENY"));
        map.put("X-ShopId", Arrays.asList("12345678"));
        map.put("X-ShardId", Arrays.asList("85"));
        map.put("X-Stats-UserId", Collections.emptyList()); // empty
        map.put("X-Stats-ApiClientId", Arrays.asList("123456"));
        map.put("X-Stats-ApiPermissionId", Arrays.asList("12345678"));
        map.put("HTTP_X_SHOPIFY_SHOP_API_CALL_LIMIT", Arrays.asList("1/40"));
        map.put("X-Shopify-Shop-Api-Call-Limit", Arrays.asList("1/40"));
        map.put("X-Shopify-API-Version", Arrays.asList("2019-04"));
        map.put("Strict-Transport-Security", Arrays.asList("max-age=7889238"));
        map.put("X-Request-Id", Arrays.asList("7ce26aaf-24f7-4e4d-b319-xxxxxxxxxxx"));
        map.put("X-Shopify-Stage", Arrays.asList("production"));
        map.put("Content-Security-Policy", Arrays.asList("default-src 'self' data: blob: 'unsafe-inline' 'unsafe-eval' https://* shopify-pos://*; block-all-mixed-content; child-src 'self' https://* shopify-pos://*; connect-src 'self' wss://* https://*; frame-ancestors 'none'; img-src 'self' data: blob: https:; script-src https://cdn.shopify.com  https://checkout.shopifycs.com  https://js-agent.newrelic.com  https://bam.nr-data.net  https://dme0ih8comzn4.cloudfront.net  https://api.stripe.com  https://mpsnare.iesnare.com  https://appcenter.intuit.com  https://www.paypal.com  https://maps.googleapis.com  https://www.google-analytics.com  https://v.shopify.com  https://widget.intercom.io  https://js.intercomcdn.com  'self' 'unsafe-inline' 'unsafe-eval'; upgrade-insecure-requests; report-uri /csp-report?source%5Baction%5D=index&source%5Bapp%5D=Shopify&source%5Bcontroller%5D=admin%2Fsettings%2Flocations&source%5Bsection%5D=admin_api&source%5Buuid%5D=7ce26aaf-24f7-4e4d-b319-e3e115da6fc7"));
        map.put("X-Content-Type-Options", Arrays.asList("nosniff"));
        map.put("X-Download-Options", Arrays.asList("noopen"));
        map.put("X-Permitted-Cross-Domain-Policies", Arrays.asList("none"));
        map.put("X-XSS-Protection", Arrays.asList("1; mode=block; report=/xss-report?source%5Baction%5D=index&source%5Bapp%5D=Shopify&source%5Bcontroller%5D=admin%2Fsettings%2Flocations&source%5Bsection%5D=admin_api&source%5Buuid%5D=7ce26aaf-24f7-4e4d-b319-xxxxxxxxx"));
        map.put("X-Dc", Arrays.asList("ash", "gcp-us-central1"));
        map.put("NEL", Arrays.asList("{\"report_to\":\"network-errors\",\"max_age\":2592000,\"failure_fraction\":0.01,\"success_fraction\":0.0001}\""));
        map.put("Report-To", Arrays.asList("{\"group\":\"network-errors\",\"max_age\":2592000,\"endpoints\":[{\"url\":\"https://monorail-edge.shopifycloud.com/v1/reports/nel/2019022225/shopify \"}]}"));
        map.put("Expect-CT", Arrays.asList("max-age=604800", "report-uri=\"https://report-uri.cloudflare.com/cdn-cgi/beacon/expect-ct \""));
        map.put("Server", Arrays.asList("cloudflare"));
        map.put("CF-RAY", Arrays.asList("4dae0dd6dadddd9572-IAD"));
        return map;
    }

    // filters
    public HeaderFilter getAutoconfigurationFilter() {
        return autoconfigurationFilter;
    }
    
    public HeaderFilter getRemoveFilter() {
        return removeFilter;
    }
    
    public HeaderFilter getReplaceFilter() {
        return replaceFilter;
    }
    
    public HeaderFilter getRemove2xFilter() {
        return remove2xFilter;
    }
    
    public HeaderFilter getReplace2xFilter() {
        return replace2xFilter;
    }

    // requests
    public Map<String, List<String>> getGetRequestHeaders() {
        return getRequestHeaders;
    }
    
    public Map<String, List<String>> getPostRequestHeaders() {
        return postRequestHeaders;
    }
    
    public Map<String, List<String>> getShopifyResponseHeaders() {
        return shopifyResponseHeaders;
    }
    
    public Map<String, List<String>> getAllRequestHeaders() {
        return allRequestHeaders;
    }
    
    public Map<String, List<String>> getAllResponseHeaders() {
        return allResponseHeaders;
    }
    
    
}
