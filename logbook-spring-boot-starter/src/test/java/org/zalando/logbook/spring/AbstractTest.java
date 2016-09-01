package org.zalando.logbook.spring;

import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@Configuration
@SpringApplicationConfiguration(Application.class)
public abstract class AbstractTest {

}
