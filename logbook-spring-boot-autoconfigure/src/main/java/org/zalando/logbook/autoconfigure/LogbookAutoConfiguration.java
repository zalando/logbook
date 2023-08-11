package org.zalando.logbook.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Logger;
import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;
import org.apache.http.client.HttpClient;
import org.apiguardian.api.API;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.SecurityFilterChain;
import org.zalando.logbook.BodyFilter;
import org.zalando.logbook.CorrelationId;
import org.zalando.logbook.HeaderFilter;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.PathFilter;
import org.zalando.logbook.QueryFilter;
import org.zalando.logbook.RequestFilter;
import org.zalando.logbook.ResponseFilter;
import org.zalando.logbook.Sink;
import org.zalando.logbook.Strategy;
import org.zalando.logbook.attributes.AttributeExtractor;
import org.zalando.logbook.core.attributes.CompositeAttributeExtractor;
import org.zalando.logbook.attributes.NoOpAttributeExtractor;
import org.zalando.logbook.core.BodyOnlyIfStatusAtLeastStrategy;
import org.zalando.logbook.core.ChunkingSink;
import org.zalando.logbook.core.Conditions;
import org.zalando.logbook.core.CurlHttpLogFormatter;
import org.zalando.logbook.core.DefaultCorrelationId;
import org.zalando.logbook.core.DefaultHttpLogFormatter;
import org.zalando.logbook.core.DefaultHttpLogWriter;
import org.zalando.logbook.core.DefaultSink;
import org.zalando.logbook.core.DefaultStrategy;
import org.zalando.logbook.core.HeaderFilters;
import org.zalando.logbook.core.PathFilters;
import org.zalando.logbook.core.QueryFilters;
import org.zalando.logbook.core.RequestFilters;
import org.zalando.logbook.core.ResponseFilters;
import org.zalando.logbook.core.SplunkHttpLogFormatter;
import org.zalando.logbook.core.StatusAtLeastStrategy;
import org.zalando.logbook.core.WithoutBodyStrategy;
import org.zalando.logbook.httpclient.LogbookHttpRequestInterceptor;
import org.zalando.logbook.httpclient.LogbookHttpResponseInterceptor;
import org.zalando.logbook.json.JacksonJsonFieldBodyFilter;
import org.zalando.logbook.json.JsonHttpLogFormatter;
import org.zalando.logbook.openfeign.FeignLogbookLogger;
import org.zalando.logbook.servlet.FormRequestMode;
import org.zalando.logbook.servlet.LogbookFilter;
import org.zalando.logbook.servlet.SecureLogbookFilter;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static jakarta.servlet.DispatcherType.ASYNC;
import static jakarta.servlet.DispatcherType.REQUEST;
import static org.apiguardian.api.API.Status.INTERNAL;
import static org.apiguardian.api.API.Status.STABLE;
import static org.zalando.logbook.autoconfigure.LogbookAutoConfiguration.JakartaServletFilterConfiguration.newFilter;
import static org.zalando.logbook.core.BodyFilters.defaultValue;
import static org.zalando.logbook.core.BodyFilters.truncate;
import static org.zalando.logbook.core.HeaderFilters.replaceHeaders;
import static org.zalando.logbook.core.QueryFilters.replaceQuery;

@API(status = STABLE)
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Logbook.class)
@EnableConfigurationProperties(LogbookProperties.class)
@AutoConfigureAfter(value = JacksonAutoConfiguration.class, name = {
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
            final AttributeExtractor attributeExtractor,
            final Sink sink) {

        return Logbook.builder()
                .condition(mergeWithExcludes(mergeWithIncludes(condition)))
                .correlationId(correlationId)
                .headerFilters(headerFilters)
                .queryFilters(queryFilters)
                .pathFilters(pathFilters)
                .bodyFilters(mergeWithTruncation(bodyFilters))
                .requestFilters(requestFilters)
                .responseFilters(responseFilters)
                .strategy(strategy)
                .attributeExtractor(attributeExtractor)
                .sink(sink)
                .build();
    }

    private Collection<BodyFilter> mergeWithTruncation(List<BodyFilter> bodyFilters) {
        final LogbookProperties.Write write = properties.getWrite();
        final int maxBodySize = write.getMaxBodySize();
        if (maxBodySize < 0) {
            return bodyFilters;
        }

        // To ensure that truncation will happen after all other body filters
        final List<BodyFilter> filters = new ArrayList<>(bodyFilters);
        final BodyFilter filter = truncate(maxBodySize);
        filters.add(filter);
        return filters;
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
                replaceQuery(new HashSet<>(parameters)::contains, properties.getObfuscate().getReplacement());
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(HeaderFilter.class)
    public HeaderFilter headerFilter() {
        final Set<String> headers = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        headers.addAll(properties.getObfuscate().getHeaders());

        return headers.isEmpty() ?
                HeaderFilters.defaultValue() :
                replaceHeaders(headers, properties.getObfuscate().getReplacement());
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(PathFilter.class)
    public PathFilter pathFilter() {
        final List<String> paths = properties.getObfuscate().getPaths();
        return paths.isEmpty() ?
                PathFilter.none() :
                paths.stream()
                        .map(path -> PathFilters.replace(path, properties.getObfuscate().getReplacement()))
                        .reduce(PathFilter::merge)
                        .orElseGet(PathFilter::none);
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(BodyFilter.class)
    @ConditionalOnProperty(value = "logbook.filters.body.default-enabled", havingValue = "true", matchIfMissing = true)
    public BodyFilter bodyFilter() {
        return defaultValue();
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(JacksonJsonFieldBodyFilter.class)
    public BodyFilter jsonBodyFieldsFilter() {
        final LogbookProperties.Obfuscate obfuscate = properties.getObfuscate();
        final List<String> jsonBodyFields = obfuscate.getJsonBodyFields();

        return new JacksonJsonFieldBodyFilter(jsonBodyFields, obfuscate.getReplacement());
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
    @ConditionalOnMissingBean(AttributeExtractor.class)
    public AttributeExtractor getAttributeExtractor(final ObjectMapper objectMapper) {
        final List<LogbookProperties.ExtractorProperty> attributeExtractors = properties.getAttributeExtractors();
        switch (attributeExtractors.size()) {
            case 0:
                return new NoOpAttributeExtractor();
            case 1:
                return attributeExtractors.get(0).toExtractor(objectMapper);
            default:
                return new CompositeAttributeExtractor(
                        attributeExtractors.stream()
                                .map(property -> property.toExtractor(objectMapper))
                                .collect(Collectors.toList())
                );
        }
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(Sink.class)
    public Sink sink(
            final HttpLogFormatter formatter,
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
    @ConditionalOnMissingBean(HttpLogFormatter.class)
    public HttpLogFormatter jsonFormatter(
            final ObjectMapper mapper) {
        return new JsonHttpLogFormatter(mapper);
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(HttpLogWriter.class)
    public HttpLogWriter writer() {
        return new DefaultHttpLogWriter();
    }

    @Bean
    @ConditionalOnMissingBean(LogbookClientHttpRequestInterceptor.class)
    public LogbookClientHttpRequestInterceptor logbookClientHttpRequestInterceptor(Logbook logbook) {
        return new LogbookClientHttpRequestInterceptor(logbook);
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
        public LogbookHttpResponseInterceptor logbookHttpResponseInterceptor(@Value("${logbook.httpclient.decompress-response:false}") final boolean decompressResponse) {
            return new LogbookHttpResponseInterceptor(decompressResponse);
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass({
            org.apache.hc.client5.http.classic.HttpClient.class,
            org.zalando.logbook.httpclient5.LogbookHttpRequestInterceptor.class,
            org.zalando.logbook.httpclient5.LogbookHttpResponseInterceptor.class
    })
    static class HttpClient5AutoConfiguration {

        @Bean
        @ConditionalOnMissingBean(org.zalando.logbook.httpclient5.LogbookHttpRequestInterceptor.class)
        public org.zalando.logbook.httpclient5.LogbookHttpRequestInterceptor logbookHttpClient5RequestInterceptor(final Logbook logbook) {
            return new org.zalando.logbook.httpclient5.LogbookHttpRequestInterceptor(logbook);
        }

        @Bean
        @ConditionalOnMissingBean(org.zalando.logbook.httpclient5.LogbookHttpResponseInterceptor.class)
        public org.zalando.logbook.httpclient5.LogbookHttpResponseInterceptor logbookHttpClient5ResponseInterceptor() {
            return new org.zalando.logbook.httpclient5.LogbookHttpResponseInterceptor();
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = Type.SERVLET)
    @ConditionalOnClass({
            Servlet.class,
            LogbookFilter.class
    })
    static class JakartaServletFilterConfiguration {

        private static final String FILTER_NAME = "logbookFilter";

        private final LogbookProperties properties;

        @API(status = INTERNAL)
        @Autowired
        public JakartaServletFilterConfiguration(final LogbookProperties properties) {
            this.properties = properties;
        }

        @Bean
        @ConditionalOnProperty(name = "logbook.filter.enabled", havingValue = "true", matchIfMissing = true)
        @ConditionalOnMissingBean(name = FILTER_NAME)
        public FilterRegistrationBean<?> logbookFilter(final Logbook logbook) {
            final LogbookFilter filter = new LogbookFilter(logbook)
                    .withFormRequestMode(properties.getFilter().getFormRequestMode());
            return newFilter(filter, FILTER_NAME, Ordered.LOWEST_PRECEDENCE);
        }

        static FilterRegistrationBean<?> newFilter(final Filter filter, final String filterName, final int order) {
            final FilterRegistrationBean<?> registration = new FilterRegistrationBean<>(filter);
            registration.setName(filterName);
            registration.setDispatcherTypes(REQUEST, ASYNC);
            registration.setOrder(order);
            return registration;
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = Type.SERVLET)
    @ConditionalOnClass({
            javax.servlet.Servlet.class,
            org.zalando.logbook.servlet.javax.LogbookFilter.class
    })
    static class JavaxServletFilterConfiguration {

        private static final String FILTER_NAME = "logbookFilter";

        private final LogbookProperties properties;

        @API(status = INTERNAL)
        @Autowired
        public JavaxServletFilterConfiguration(final LogbookProperties properties) {
            this.properties = properties;
        }

        @Bean
        @ConditionalOnProperty(name = "logbook.filter.enabled", havingValue = "true", matchIfMissing = true)
        @ConditionalOnMissingBean(name = FILTER_NAME)
        public org.zalando.logbook.servlet.javax.LogbookFilter logbookFilter(final Logbook logbook) {
            FormRequestMode fromProperties = properties.getFilter().getFormRequestMode();
            org.zalando.logbook.servlet.javax.FormRequestMode formRequestMode = org.zalando.logbook.servlet.javax.FormRequestMode.valueOf(fromProperties.name());
            return new org.zalando.logbook.servlet.javax.LogbookFilter(logbook)
                    .withFormRequestMode(formRequestMode);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass({
            SecurityFilterChain.class,
            Servlet.class,
            LogbookFilter.class
    })
    @ConditionalOnWebApplication(type = Type.SERVLET)
    @AutoConfigureAfter(name = {
            "org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration" // Spring Boot 2.x
    })
    static class JakartaSecurityServletFilterConfiguration {

        private static final String FILTER_NAME = "secureLogbookFilter";

        @Bean
        @ConditionalOnProperty(name = "logbook.secure-filter.enabled", havingValue = "true", matchIfMissing = true)
        @ConditionalOnMissingBean(name = FILTER_NAME)
        public FilterRegistrationBean<?> secureLogbookFilter(final Logbook logbook) {
            return newFilter(new SecureLogbookFilter(logbook), FILTER_NAME, Ordered.HIGHEST_PRECEDENCE + 1);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass({
            SecurityFilterChain.class,
            javax.servlet.Servlet.class,
            org.zalando.logbook.servlet.javax.LogbookFilter.class
    })
    @ConditionalOnWebApplication(type = Type.SERVLET)
    @AutoConfigureAfter(name = {
            "org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration" // Spring Boot 2.x
    })
    static class JavaxSecurityServletFilterConfiguration {

        private static final String FILTER_NAME = "secureLogbookFilter";

        @Bean
        @ConditionalOnProperty(name = "logbook.secure-filter.enabled", havingValue = "true", matchIfMissing = true)
        @ConditionalOnMissingBean(name = FILTER_NAME)
        @Order(Ordered.HIGHEST_PRECEDENCE + 1)
        public org.zalando.logbook.servlet.javax.SecureLogbookFilter secureLogbookFilter(final Logbook logbook) {
            return new org.zalando.logbook.servlet.javax.SecureLogbookFilter(logbook);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass({
            Logger.class,
            FeignLogbookLogger.class
    })
    static class FeignLogbookLoggerConfiguration {

        @Bean
        @ConditionalOnMissingBean(Logger.class)
        public Logger feignLogbookLogger(Logbook logbook) {
            return new FeignLogbookLogger(logbook);
        }
    }
}
