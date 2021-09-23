package org.zalando.logbook.spring.webflux;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author sokomishalov
 */
@SpringBootApplication
public class TestApplication {

    @RestController
    public static class TestController {

        @PostMapping("/echo")
        public Mono<String> echo(@RequestBody(required = false) Mono<String> body) {
            return body;
        }

        @PostMapping("/discard")
        @SuppressWarnings("unused")
        public Mono<Void> discard(@RequestBody(required = false) Mono<String> body) {
            return body.then(Mono.empty());
        }
    }
}
