package org.zalando.logbook.spring;

import org.apache.http.client.HttpClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.SecurityFilterAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.httpclient.LogbookHttpRequestInterceptor;
import org.zalando.logbook.httpclient.LogbookHttpResponseInterceptor;

@Configuration
@ConditionalOnClass({
        HttpClient.class,
        LogbookHttpRequestInterceptor.class,
        LogbookHttpResponseInterceptor.class
})
@ConditionalOnBean(Logbook.class)
@AutoConfigureAfter({
        LogbookAutoConfiguration.class,
})
public class LogbookHttpClientAutoConfiguration {

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
