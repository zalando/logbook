package org.zalando.logbook.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.HttpClient;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.web.SecurityFilterChain;
import org.zalando.logbook.BodyFilter;
import org.zalando.logbook.ChunkingHttpLogWriter;
import org.zalando.logbook.Conditions;
import org.zalando.logbook.CurlHttpLogFormatter;
import org.zalando.logbook.DefaultHttpLogFormatter;
import org.zalando.logbook.DefaultHttpLogWriter;
import org.zalando.logbook.HeaderFilter;
import org.zalando.logbook.HeaderFilters;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.JsonHttpLogFormatter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.QueryFilter;
import org.zalando.logbook.QueryFilters;
import org.zalando.logbook.RawHttpRequest;
import org.zalando.logbook.RawRequestFilter;
import org.zalando.logbook.RawRequestFilters;
import org.zalando.logbook.RawResponseFilter;
import org.zalando.logbook.RawResponseFilters;
import org.zalando.logbook.RequestFilter;
import org.zalando.logbook.ResponseFilter;
import org.zalando.logbook.httpclient.LogbookHttpRequestInterceptor;
import org.zalando.logbook.httpclient.LogbookHttpResponseInterceptor;
import org.zalando.logbook.servlet.LogbookFilter;
import org.zalando.logbook.servlet.Strategy;

import javax.servlet.Filter;
import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static javax.servlet.DispatcherType.ASYNC;
import static javax.servlet.DispatcherType.ERROR;
import static javax.servlet.DispatcherType.REQUEST;
import static org.apiguardian.api.API.Status.INTERNAL;
import static org.apiguardian.api.API.Status.STABLE;
import static org.zalando.logbook.BodyFilters.defaultValue;
import static org.zalando.logbook.BodyFilters.truncate;

@API(status = STABLE)
@Configuration
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
            final Predicate<RawHttpRequest> condition,
            final List<RawRequestFilter> rawRequestFilters,
            final List<RawResponseFilter> rawResponseFilters,
            final List<HeaderFilter> headerFilters,
            final List<QueryFilter> queryFilters,
            final List<BodyFilter> bodyFilters,
            final List<RequestFilter> requestFilters,
            final List<ResponseFilter> responseFilters,
            @SuppressWarnings("SpringJavaAutowiringInspection") final HttpLogFormatter formatter,
            final HttpLogWriter writer) {
        return Logbook.builder()
                .condition(mergeWithExcludes(condition))
                .rawRequestFilters(rawRequestFilters)
                .rawResponseFilters(rawResponseFilters)
                .headerFilters(headerFilters)
                .queryFilters(queryFilters)
                .bodyFilters(bodyFilters)
                .requestFilters(requestFilters)
                .responseFilters(responseFilters)
                .formatter(formatter)
                .writer(writer)
                .build();
    }

    private Predicate<RawHttpRequest> mergeWithExcludes(final Predicate<RawHttpRequest> predicate) {
        return properties.getExclude().stream()
                .map(Conditions::<RawHttpRequest>requestTo)
                .map(Predicate::negate)
                .reduce(predicate, Predicate::and);
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(name = "requestCondition")
    public Predicate<RawHttpRequest> requestCondition() {
        return $ -> true;
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(RawRequestFilter.class)
    public RawRequestFilter rawRequestFilter() {
        return RawRequestFilters.defaultValue();
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(RawResponseFilter.class)
    public RawResponseFilter rawResponseFilter() {
        return RawResponseFilters.defaultValue();
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(QueryFilter.class)
    public QueryFilter queryFilter() {
        final List<String> parameters = properties.getObfuscate().getParameters();
        return parameters.isEmpty() ?
                QueryFilters.defaultValue() :
                parameters.stream()
                        .map(parameter -> QueryFilters.replaceQuery(parameter, "XXX"))
                        .collect(toList()).stream()
                        .reduce(QueryFilter::merge)
                        .orElseGet(QueryFilter::none);
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(HeaderFilter.class)
    public HeaderFilter headerFilter() {
        final List<String> headers = properties.getObfuscate().getHeaders();
        return headers.isEmpty() ?
                HeaderFilters.defaultValue() :
                headers.stream()
                        .map(header -> HeaderFilters.replaceHeaders(header::equalsIgnoreCase, "XXX"))
                        .collect(toList()).stream()
                        .reduce(HeaderFilter::merge)
                        .orElseGet(HeaderFilter::none);
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

        return BodyFilter.merge(truncate(maxBodySize), defaultValue());
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(RequestFilter.class)
    public RequestFilter requestFilter() {
        return RequestFilter.none();
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(ResponseFilter.class)
    public ResponseFilter responseFilter() {
        return ResponseFilter.none();
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
    @ConditionalOnBean(ObjectMapper.class)
    @ConditionalOnMissingBean(HttpLogFormatter.class)
    public HttpLogFormatter jsonFormatter(
            @SuppressWarnings("SpringJavaAutowiringInspection") final ObjectMapper mapper) {
        return new JsonHttpLogFormatter(mapper);
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(HttpLogWriter.class)
    public HttpLogWriter writer(final Logger httpLogger) {
        final LogbookProperties.Write write = properties.getWrite();
        final Level level = write.getLevel();
        final int size = write.getChunkSize();

        final HttpLogWriter writer = new DefaultHttpLogWriter(httpLogger, level);
        return size > 0 ? new ChunkingHttpLogWriter(size, writer) : writer;
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(name = "httpLogger")
    public Logger httpLogger() {
        return LoggerFactory.getLogger(properties.getWrite().getCategory());
    }

    @Configuration
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

    }

    @Configuration
    @ConditionalOnClass(Filter.class)
    @ConditionalOnWebApplication
    static class ServletFilterConfiguration {

        private static final String FILTER_NAME = "authorizedLogbookFilter";

        @Bean
        @ConditionalOnProperty(name = "logbook.filter.enabled", havingValue = "true", matchIfMissing = true)
        @ConditionalOnMissingBean(name = FILTER_NAME)
        public FilterRegistrationBean authorizedLogbookFilter(final Logbook logbook) {
            final Filter filter = new LogbookFilter(logbook);
            @SuppressWarnings("unchecked") // as of Spring Boot 2.x
            final FilterRegistrationBean registration = new FilterRegistrationBean(filter);
            registration.setName(FILTER_NAME);
            registration.setDispatcherTypes(REQUEST, ASYNC, ERROR);
            registration.setOrder(Ordered.LOWEST_PRECEDENCE);
            return registration;
        }

    }

    @Configuration
    @ConditionalOnClass(SecurityFilterChain.class)
    @ConditionalOnWebApplication
    @AutoConfigureAfter(name = {
            "org.springframework.boot.autoconfigure.security.SecurityFilterAutoConfiguration", // Spring Boot 1.x
            "org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration" // Spring Boot 2.x
    })
    static class SecurityServletFilterConfiguration {

        private static final String FILTER_NAME = "unauthorizedLogbookFilter";

        @Bean
        @ConditionalOnProperty(name = "logbook.filter.enabled", havingValue = "true", matchIfMissing = true)
        @ConditionalOnMissingBean(name = FILTER_NAME)
        public FilterRegistrationBean unauthorizedLogbookFilter(final Logbook logbook) {
            final Filter filter = new LogbookFilter(logbook, Strategy.SECURITY);
            @SuppressWarnings("unchecked") // as of Spring Boot 2.x
            final FilterRegistrationBean registration = new FilterRegistrationBean(filter);
            registration.setName(FILTER_NAME);
            registration.setDispatcherTypes(REQUEST, ASYNC, ERROR);
            registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
            return registration;
        }

    }


}
