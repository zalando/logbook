package org.zalando.logbook.autoconfigure;

import lombok.AllArgsConstructor;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

@AllArgsConstructor
public class LogbookRestTemplateCustomizer implements RestTemplateCustomizer {

    private final LogbookClientHttpRequestInterceptor logbookClientHttpRequestInterceptor;

    @Override
    public void customize(RestTemplate restTemplate) {
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(restTemplate.getRequestFactory()));
        restTemplate.getInterceptors().add(logbookClientHttpRequestInterceptor);
    }
}
