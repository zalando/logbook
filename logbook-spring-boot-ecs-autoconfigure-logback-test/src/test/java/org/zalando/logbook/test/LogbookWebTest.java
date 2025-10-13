package org.zalando.logbook.test;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Import(Object.class)
@Target(TYPE)
@Inherited
@Retention(RUNTIME)
@Documented
@ActiveProfiles
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = LogbookWebTest.Application.class)
@TestPropertySource
public @interface LogbookWebTest {

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
