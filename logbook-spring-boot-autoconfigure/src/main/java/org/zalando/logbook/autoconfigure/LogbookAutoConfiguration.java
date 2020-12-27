package org.zalando.logbook.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.HttpClient;
import org.apiguardian.api.API;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.security.web.SecurityFilterChain;
import org.zalando.logbook.*;
import org.zalando.logbook.autoconfigure.interceptors.LogbookClientHttpRequestInterceptor;
import org.zalando.logbook.httpclient.LogbookHttpRequestInterceptor;
import org.zalando.logbook.httpclient.LogbookHttpResponseInterceptor;
import org.zalando.logbook.json.JsonHttpLogFormatter;
import org.zalando.logbook.servlet.LogbookFilter;
import org.zalando.logbook.servlet.SecureLogbookFilter;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

import static javax.servlet.DispatcherType.ASYNC;
import static javax.servlet.DispatcherType.REQUEST;
import static org.apiguardian.api.API.Status.INTERNAL;
import static org.apiguardian.api.API.Status.STABLE;
import static org.zalando.logbook.BodyFilters.defaultValue;
import static org.zalando.logbook.BodyFilters.truncate;
import static org.zalando.logbook.HeaderFilters.replaceHeaders;
import static org.zalando.logbook.QueryFilters.replaceQuery;
import static org.zalando.logbook.autoconfigure.LogbookAutoConfiguration.ServletFilterConfiguration.newFilter;

@API(status = STABLE)
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Logbook.class)
@EnableConfigurationProperties(LogbookProperties.class)
@AutoConfigureAfter(value = JacksonAutoConfiguration.class, name = {
        "org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration", // Spring Boot 1.x
        "org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration" // Spring Boot 2.x
})
public class LogbookAutoConfiguration {

    private final LogbookProperties properties;

    @API(status = INTERNAL)
    @Autowired
    public LogbookAutoConfiguration(final LogbookProperties properties) {
        this.properties = properties;
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(Logbook.class)
    public Logbook logbook(
            final Predicate<HttpRequest> condition,
            final CorrelationId correlationId,
            final List<HeaderFilter> headerFilters,
            final List<PathFilter> pathFilters,
            final List<QueryFilter> queryFilters,
            final List<BodyFilter> bodyFilters,
            final List<RequestFilter> requestFilters,
            final List<ResponseFilter> responseFilters,
            final Strategy strategy,
            final Sink sink) {

        return Logbook.builder()
                .condition(mergeWithExcludes(mergeWithIncludes(condition)))
                .correlationId(correlationId)
                .headerFilters(headerFilters)
                .queryFilters(queryFilters)
                .pathFilters(pathFilters)
                .bodyFilters(bodyFilters)
                .requestFilters(requestFilters)
                .responseFilters(responseFilters)
                .strategy(strategy)
                .sink(sink)
                .build();
    }

    private Predicate<HttpRequest> mergeWithExcludes(final Predicate<HttpRequest> predicate) {
        return properties.getExclude().stream()
                .map(Conditions::requestTo)
                .map(Predicate::negate)
                .reduce(predicate, Predicate::and);
    }

    private Predicate<HttpRequest> mergeWithIncludes(final Predicate<HttpRequest> predicate) {
        return properties.getInclude().stream()
                .map(Conditions::requestTo)
                .reduce(Predicate::or)
                .map(predicate::and)
                .orElse(predicate);
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(name = "requestCondition")
    public Predicate<HttpRequest> requestCondition() {
        return $ -> true;
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(CorrelationId.class)
    public CorrelationId correlationId() {
        return new DefaultCorrelationId();
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(QueryFilter.class)
    public QueryFilter queryFilter() {
        final List<String> parameters = properties.getObfuscate().getParameters();
        return parameters.isEmpty() ?
                QueryFilters.defaultValue() :
                replaceQuery(new HashSet<>(parameters)::contains, "XXX");
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(HeaderFilter.class)
    public HeaderFilter headerFilter() {
        final Set<String> headers = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        headers.addAll(properties.getObfuscate().getHeaders());

        return headers.isEmpty() ?
                HeaderFilters.defaultValue() :
                replaceHeaders(headers, "XXX");
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(PathFilter.class)
    public PathFilter pathFilter() {
        final List<String> paths = properties.getObfuscate().getPaths();
        return paths.isEmpty() ?
                PathFilter.none() :
                paths.stream()
                        .map(path -> PathFilters.replace(path, "XXX"))
                        .reduce(PathFilter::merge)
                        .orElseGet(PathFilter::none);
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(BodyFilter.class)
    public BodyFilter bodyFilter() {
        final LogbookProperties.Write write = properties.getWrite();
        final int maxBodySize = write.getMaxBodySize();

        if (maxBodySize < 0) {
            return defaultValue();
        }

        return BodyFilter.merge(defaultValue(), truncate(maxBodySize));
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(RequestFilter.class)
    public RequestFilter requestFilter() {
        return RequestFilters.defaultValue();
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(ResponseFilter.class)
    public ResponseFilter responseFilter() {
        return ResponseFilters.defaultValue();
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(Strategy.class)
    @ConditionalOnProperty(name = "logbook.strategy", havingValue = "default", matchIfMissing = true)
    public Strategy strategy() {
        return new DefaultStrategy();
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(Strategy.class)
    @ConditionalOnProperty(name = "logbook.strategy", havingValue = "status-at-least")
    public Strategy statusAtLeastStrategy(@Value("${logbook.minimum-status:400}") final int status) {
        return new StatusAtLeastStrategy(status);
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(Strategy.class)
    @ConditionalOnProperty(name = "logbook.strategy", havingValue = "body-only-if-status-at-least")
    public Strategy bodyOnlyIfStatusAtLeastStrategy(@Value("${logbook.minimum-status:400}") final int status) {
        return new BodyOnlyIfStatusAtLeastStrategy(status);
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(Strategy.class)
    @ConditionalOnProperty(name = "logbook.strategy", havingValue = "without-body")
    public Strategy withoutBody() {
        return new WithoutBodyStrategy();
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(Sink.class)
    public Sink sink(
            @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") final HttpLogFormatter formatter,
            final HttpLogWriter writer) {
        return new DefaultSink(formatter, writer);
    }

    @API(status = INTERNAL)
    @Bean
    @Primary
    @ConditionalOnBean(Sink.class)
    @ConditionalOnProperty("logbook.write.chunk-size")
    public Sink chunkingSink(final Sink sink) {
        return new ChunkingSink(sink, properties.getWrite().getChunkSize());
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(HttpLogFormatter.class)
    @ConditionalOnProperty(name = "logbook.format.style", havingValue = "http")
    public HttpLogFormatter httpFormatter() {
        return new DefaultHttpLogFormatter();
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(HttpLogFormatter.class)
    @ConditionalOnProperty(name = "logbook.format.style", havingValue = "curl")
    public HttpLogFormatter curlFormatter() {
        return new CurlHttpLogFormatter();
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(HttpLogFormatter.class)
    @ConditionalOnProperty(name = "logbook.format.style", havingValue = "splunk")
    public HttpLogFormatter splunkHttpLogFormatter() {
        return new SplunkHttpLogFormatter();
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnBean(ObjectMapper.class)
    @ConditionalOnMissingBean(HttpLogFormatter.class)
    public HttpLogFormatter jsonFormatter(
            @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") final ObjectMapper mapper) {
        return new JsonHttpLogFormatter(mapper);
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(HttpLogWriter.class)
    public HttpLogWriter writer() {
        return new DefaultHttpLogWriter();
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass({
            HttpClient.class,
            LogbookHttpRequestInterceptor.class,
            LogbookHttpResponseInterceptor.class
    })
    static class HttpClientAutoConfiguration {

        @Bean
        @ConditionalOnMissingBean(LogbookHttpRequestInterceptor.class)
        public LogbookHttpRequestInterceptor logbookHttpRequestInterceptor(final Logbook logbook) {
            return new LogbookHttpRequestInterceptor(logbook);
        }

        @Bean
        @ConditionalOnMissingBean(LogbookHttpResponseInterceptor.class)
        public LogbookHttpResponseInterceptor logbookHttpResponseInterceptor() {
            return new LogbookHttpResponseInterceptor();
        }

        @Bean
        @ConditionalOnMissingBean(LogbookClientHttpRequestInterceptor.class)
        public LogbookClientHttpRequestInterceptor logbookClientHttpRequestInterceptor(final LogbookHttpRequestInterceptor requestInterceptor, final LogbookHttpResponseInterceptor responseInterceptor) {
            return new LogbookClientHttpRequestInterceptor(requestInterceptor, responseInterceptor);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = Type.SERVLET)
    @ConditionalOnClass({
            Servlet.class,
            LogbookFilter.class
    })
    static class ServletFilterConfiguration {

        private static final String FILTER_NAME = "logbookFilter";

        private final LogbookProperties properties;

        @API(status = INTERNAL)
        @Autowired
        public ServletFilterConfiguration(final LogbookProperties properties) {
            this.properties = properties;
        }

        @Bean
        @ConditionalOnProperty(name = "logbook.filter.enabled", havingValue = "true", matchIfMissing = true)
        @ConditionalOnMissingBean(name = FILTER_NAME)
        public FilterRegistrationBean logbookFilter(final Logbook logbook) {
            final LogbookFilter filter = new LogbookFilter(logbook)
                    .withFormRequestMode(properties.getFilter().getFormRequestMode());
            return newFilter(filter, FILTER_NAME, Ordered.LOWEST_PRECEDENCE);
        }

        static FilterRegistrationBean newFilter(final Filter filter, final String filterName, final int order) {
            @SuppressWarnings("unchecked") // as of Spring Boot 2.x
            final FilterRegistrationBean registration = new FilterRegistrationBean(filter);
            registration.setName(filterName);
            registration.setDispatcherTypes(REQUEST, ASYNC);
            registration.setOrder(order);
            return registration;
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(SecurityFilterChain.class)
    @ConditionalOnWebApplication(type = Type.SERVLET)
    @AutoConfigureAfter(name = {
            "org.springframework.boot.autoconfigure.security.SecurityFilterAutoConfiguration", // Spring Boot 1.x
            "org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration" // Spring Boot 2.x
    })
    static class SecurityServletFilterConfiguration {

        private static final String FILTER_NAME = "secureLogbookFilter";

        @Bean
        @ConditionalOnProperty(name = "logbook.secure-filter.enabled", havingValue = "true", matchIfMissing = true)
        @ConditionalOnMissingBean(name = FILTER_NAME)
        public FilterRegistrationBean secureLogbookFilter(final Logbook logbook) {
            return newFilter(new SecureLogbookFilter(logbook), FILTER_NAME, Ordered.HIGHEST_PRECEDENCE + 1);
        }
    }
}
