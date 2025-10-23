package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesRegex;

@ExtendWith({NativeEcsLoggingExtension.class, OutputCaptureExtension.class})
@LogbookWebTest(profiles = "ecs", imports = NativeEcsStructuredHttpLogFormatterTest.Configuration.class)
public class NativeEcsStructuredHttpLogFormatterTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RestClient restClient;

    @Test
    void shouldLogInEcsFormat(CapturedOutput capturedOutput) {
        // given
        RestClient.RequestHeadersSpec<?> requestHeadersSpec = restClient.get().uri(builder -> builder.scheme("http").host("localhost").port(port).path("/api/v1/test").build()).header("Accept", MediaType.APPLICATION_JSON_VALUE);

        // when
        ResponseEntity<Map<String, Boolean>> responseEntity = requestHeadersSpec.retrieve().toEntity(new ParameterizedTypeReference<>() {
        });

        // then
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody(), hasEntry("test", true));

        List<String> logLines = Arrays.asList(capturedOutput.getOut().split("\\R"));
        assertThat(logLines, hasItem(matchesRegex("\\{\"@timestamp\":\".*?\"," +
                "\"log\":\\{\"level\":\"TRACE\",\"logger\":\"org\\.zalando\\.logbook\\.Logbook\"\\}," +
                "\"process\":\\{\"pid\":\\d+,\"thread\":\\{\"name\":\"main\"\\}\\}," +
                "\"service\":\\{\"name\":\"ecs\",\"node\":\\{\"name\":\"ecs-1\"\\}\\}," +
                "\"message\":\"Request to http://localhost:\\d+/api/v1/test\"," +
                "\"ecs\":\\{\"version\":\"8\\.11\"\\}," +
                "\"http\":\\{\"version\":\"1\\.1\",\"request\":\\{\"id\":\"[a-f0-9]+\",\"method\":\"GET\"\\}\\}," +
                "\"event\":\\{\"kind\":\"event\",\"category\":\"web\",\"type\":\"start\",\"action\":\"http-request\",\"outcome\":\"unknown\"\\}," +
                "\"url\":\\{\"full\":\"http://localhost:\\d+/api/v1/test\",\"scheme\":\"http\",\"domain\":\"localhost\",\"path\":\"/api/v1/test\",\"port\":\\d+\\}\\}")));
        assertThat(logLines, hasItem(matchesRegex("\\{\"@timestamp\":\".*?\"," +
                "\"log\":\\{\"level\":\"TRACE\",\"logger\":\"org\\.zalando\\.logbook\\.Logbook\"\\}," +
                "\"process\":\\{\"pid\":\\d+,\"thread\":\\{\"name\":\"http-nio-auto.*?\"\\}\\}," +
                "\"service\":\\{\"name\":\"ecs\",\"node\":\\{\"name\":\"ecs-1\"\\}\\}," +
                "\"message\":\"Request to http://localhost:\\d+/api/v1/test\"," +
                "\"ecs\":\\{\"version\":\"8\\.11\"\\}," +
                "\"http\":\\{\"version\":\"1\\.1\",\"request\":\\{\"id\":\"[a-f0-9]+\",\"method\":\"GET\"\\}\\}," +
                "\"event\":\\{\"kind\":\"event\",\"category\":\"web\",\"type\":\"start\",\"action\":\"http-request\",\"outcome\":\"unknown\"\\}," +
                "\"url\":\\{\"full\":\"http://localhost:\\d+/api/v1/test\",\"scheme\":\"http\",\"domain\":\"localhost\",\"path\":\"/api/v1/test\",\"port\":\\d+\\}\\}")));
        // TODO
    }

    @TestConfiguration
    static class Configuration {

        @Bean
        public RestClient restClient(LogbookClientHttpRequestInterceptor logbookClientHttpRequestInterceptor) {
            return RestClient.builder().requestInterceptor(logbookClientHttpRequestInterceptor).build();
        }

        @RestController
        @RequestMapping("/api/v1")
        static class TestRestController {

            @GetMapping(path = "/test", produces = MediaType.APPLICATION_JSON_VALUE)
            public ResponseEntity<Map<String, Boolean>> test() {
                return ResponseEntity.ok(Map.of("test", true));
            }

        }

    }

}
