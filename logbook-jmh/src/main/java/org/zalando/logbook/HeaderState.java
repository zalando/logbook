package org.zalando.logbook;

import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.zalando.logbook.autoconfigure.LogbookAutoConfiguration;
import org.zalando.logbook.autoconfigure.LogbookProperties;

import java.util.Arrays;
import java.util.UUID;

import static java.util.Collections.emptyList;

@State(Scope.Benchmark)
public class HeaderState {

    private HttpHeaders shopifyResponseHeaders; // real-life example

    private HttpHeaders postRequestHeaders;
    private HttpHeaders getRequestHeaders;

    private HttpHeaders allResponseHeaders;
    private HttpHeaders allRequestHeaders;

    // filters
    private HeaderFilter autoconfigurationFilter;
    private HeaderFilter replaceFilter;
    private HeaderFilter replace2xFilter;
    private HeaderFilter removeFilter;
    private HeaderFilter remove2xFilter;

    @Setup(Level.Trial)
    public void setUp() throws Exception {
        final LogbookProperties properties = new LogbookProperties();
        final LogbookAutoConfiguration ac = new LogbookAutoConfiguration(properties);

        autoconfigurationFilter = ac.headerFilter();
        replaceFilter = HeaderFilters.authorization();
        removeFilter = HeaderFilters.removeHeaders("Authorization");

        replace2xFilter = HeaderFilter.merge(
                HeaderFilters.replaceHeaders("Authorization", "XXX"),
                HeaderFilters.replaceHeaders("Set-Cookie", "XXX"));

        remove2xFilter = HeaderFilter.merge(
                HeaderFilters.removeHeaders("Authorization"),
                HeaderFilters.removeHeaders("Set-Cookie"));

        // header collections
        postRequestHeaders = postRequestHeaders();
        getRequestHeaders = getRequestHeaders();

        final HttpHeaders responseHeaders = getContentResponseHeaders();

        final HttpHeaders istioHeaders = defaultIstioHeaders();
        final HttpHeaders securityResponseHeaders = defaultSpringSecurityResponseHeaders();
        final HttpHeaders defaultOpenIdConnectHeaders = defaultOpenIdConnectHeaders();

        allRequestHeaders = HttpHeaders.empty();
        allRequestHeaders = allRequestHeaders.update(postRequestHeaders);
        allRequestHeaders = allRequestHeaders.update(istioHeaders);
        allRequestHeaders = allRequestHeaders.update(defaultOpenIdConnectHeaders);

        allResponseHeaders = HttpHeaders.empty();
        allResponseHeaders = allResponseHeaders.update(responseHeaders);
        allResponseHeaders = allResponseHeaders.update(istioHeaders);
        allResponseHeaders = allResponseHeaders.update(securityResponseHeaders);

        shopifyResponseHeaders = defaultShopifyResponse();
    }

    protected HttpHeaders postRequestHeaders() {
        return HttpHeaders.empty()
                .update("Accept-Encoding", "gzip,deflate")
                .update("Accept", "application/json")
                .update("User-Agent", "Apache-HttpClient/x.y.z (Java/1.8.0_xxx)")
                .update("Host", "https://github.com")
                .update("Content-Type", "application/json")
                .update("Content-Length", "256");
    }

    protected static HttpHeaders getRequestHeaders() {
        return HttpHeaders.empty()
                .update("Accept-Encoding", "gzip,deflate")
                .update("Accept", "application/json")
                .update("User-Agent", "Apache-HttpClient/x.y.z (Java/1.8.0_xxx)")
                .update("Host", "https://github.com");
    }

    protected static HttpHeaders getContentResponseHeaders() {
        return HttpHeaders.empty()
                .update("Content-Type", "application/json")
                .update("Content-Length", "256")
                .update("Transfer-Encoding", "chunked")
                .update("Connection", "keep-alive");
    }

    protected static HttpHeaders defaultSpringSecurityResponseHeaders() {
        // https://docs.spring.io/spring-security/site/docs/5.0.x/reference/html/headers.html
        return HttpHeaders.empty()
                .update("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate")
                .update("Pragma", "no-cache")
                .update("Expires", "0")
                .update("X-Content-Type-Options", "nosniff")
                .update("Strict-Transport-Security", "max-age=31536000 ; includeSubDomains")
                .update("X-Frame-Options", "DENY")
                .update("X-XSS-Protection", "1; mode=block");
    }

    protected HttpHeaders defaultOpenIdConnectHeaders() {
        return HttpHeaders.empty()
                .update("Authorization", UUID.randomUUID().toString());
    }

    protected HttpHeaders defaultIstioHeaders() {
        return HttpHeaders.empty()
                // HTTP header names
                .update("x-request-id", UUID.randomUUID().toString())
                .update("x-b3-traceid", UUID.randomUUID().toString())
                .update("x-b3-spanid", UUID.randomUUID().toString())
                .update("x-b3-parentspanid", "1")
                .update("x-b3-flags", UUID.randomUUID().toString())
                .update("x-ot-span-context", UUID.randomUUID().toString());
    }

    protected HttpHeaders defaultShopifyResponse() {
        return HttpHeaders.empty()
                .update("Content-Type", "application/json; charset=utf-8")
                .update("Transfer-Encoding", "chunked")
                .update("Connection", "keep-alive")
                .update("Set-Cookie",
                        "__cfduid=db9ccc357feeef085ffff7ad8b6deee870b15ggg20390; expires=Thu, 21-May-20 10:19:50 GMT; path=/; domain=.xxx.myshopify.com; HttpOnly")
                .update("X-Sorting-Hat-PodId", "85")
                .update("X-Sorting-Hat-ShopId", "123456789012")
                .update("Vary", "Accept-Encoding")
                .update("Referrer-Policy", "origin-when-cross-origin")
                .update("X-Frame-Options", "DENY")
                .update("X-ShopId", "12345678")
                .update("X-ShardId", "85")
                .update("X-Stats-UserId", emptyList()) // empty
                .update("X-Stats-ApiClientId", "123456")
                .update("X-Stats-ApiPermissionId", "12345678")
                .update("HTTP_X_SHOPIFY_SHOP_API_CALL_LIMIT", "1/40")
                .update("X-Shopify-Shop-Api-Call-Limit", "1/40")
                .update("X-Shopify-API-Version", "2019-04")
                .update("Strict-Transport-Security", "max-age=7889238")
                .update("X-Request-Id", "7ce26aaf-24f7-4e4d-b319-xxxxxxxxxxx")
                .update("X-Shopify-Stage", "production")
                .update("Content-Security-Policy",
                        "default-src 'self' data: blob: 'unsafe-inline' 'unsafe-eval' https://* shopify-pos://*; block-all-mixed-content; child-src 'self' https://* shopify-pos://*; connect-src 'self' wss://* https://*; frame-ancestors 'none'; img-src 'self' data: blob: https:; script-src https://cdn.shopify.com  https://checkout.shopifycs.com  https://js-agent.newrelic.com  https://bam.nr-data.net  https://dme0ih8comzn4.cloudfront.net  https://api.stripe.com  https://mpsnare.iesnare.com  https://appcenter.intuit.com  https://www.paypal.com  https://maps.googleapis.com  https://www.google-analytics.com  https://v.shopify.com  https://widget.intercom.io  https://js.intercomcdn.com  'self' 'unsafe-inline' 'unsafe-eval'; upgrade-insecure-requests; report-uri /csp-report?source%5Baction%5D=index&source%5Bapp%5D=Shopify&source%5Bcontroller%5D=admin%2Fsettings%2Flocations&source%5Bsection%5D=admin_api&source%5Buuid%5D=7ce26aaf-24f7-4e4d-b319-e3e115da6fc7")
                .update("X-Content-Type-Options", "nosniff")
                .update("X-Download-Options", "noopen")
                .update("X-Permitted-Cross-Domain-Policies", "none")
                .update("X-XSS-Protection",
                        "1; mode=block; report=/xss-report?source%5Baction%5D=index&source%5Bapp%5D=Shopify&source%5Bcontroller%5D=admin%2Fsettings%2Flocations&source%5Bsection%5D=admin_api&source%5Buuid%5D=7ce26aaf-24f7-4e4d-b319-xxxxxxxxx")
                .update("X-Dc", Arrays.asList("ash", "gcp-us-central1"))
                .update("NEL",
                        "{\"report_to\":\"network-errors\",\"max_age\":2592000,\"failure_fraction\":0.01,\"success_fraction\":0.0001}\"")
                .update("Report-To",
                        "{\"group\":\"network-errors\",\"max_age\":2592000,\"endpoints\":[{\"url\":\"https://monorail-edge.shopifycloud.com/v1/reports/nel/2019022225/shopify \"}]}")
                .update("Expect-CT", Arrays.asList("max-age=604800",
                        "report-uri=\"https://report-uri.cloudflare.com/cdn-cgi/beacon/expect-ct \""))
                .update("Server", "cloudflare")
                .update("CF-RAY", "4dae0dd6dadddd9572-IAD");
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
    public HttpHeaders getGetRequestHeaders() {
        return getRequestHeaders;
    }

    public HttpHeaders getPostRequestHeaders() {
        return postRequestHeaders;
    }

    public HttpHeaders getShopifyResponseHeaders() {
        return shopifyResponseHeaders;
    }

    public HttpHeaders getAllRequestHeaders() {
        return allRequestHeaders;
    }

    public HttpHeaders getAllResponseHeaders() {
        return allResponseHeaders;
    }


}
