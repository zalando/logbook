package org.zalando.logbook.spring;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
@Documented
@Inherited
@SpringBootTest
@ActiveProfiles
@TestPropertySource
@WebAppConfiguration
@Import(Void.class)
@ExtendWith(SpringExtension.class)
public @interface LogbookTest {

    @AliasFor(annotation = ActiveProfiles.class, attribute = "profiles")
    String[] profiles() default {};

    @AliasFor(annotation = Import.class, attribute = "value")
    Class<?>[] imports() default {};

    @AliasFor(annotation = TestPropertySource.class, attribute = "properties")
    String[] properties() default {""};

    @SpringBootApplication
    class Application {
    }
}
