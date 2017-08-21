package org.zalando.logbook.spring;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Configuration
@SpringBootTest(classes = Application.class)
public abstract class AbstractTest {

}
