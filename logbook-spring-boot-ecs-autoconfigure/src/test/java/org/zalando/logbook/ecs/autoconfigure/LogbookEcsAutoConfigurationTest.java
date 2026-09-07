package org.zalando.logbook.ecs.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.Sink;
import org.zalando.logbook.ecs.DefaultEcsSink;
import org.zalando.logbook.ecs.HttpStatusCodeBasedEcsSink;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class LogbookEcsAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(LogbookEcsAutoConfiguration.class)
            .withPropertyValues("logging.structured.format.console=ecs");

    @Test
    void shouldBackOffWhenSinkBeanExists() {
        contextRunner
                .withBean("customSink", Sink.class, CustomSink::new)
                .run(context -> {
                    assertThat(context).hasSingleBean(Sink.class);
                    assertThat(context).doesNotHaveBean("ecsSink");
                    assertThat(context.getBean(Sink.class)).isInstanceOf(CustomSink.class);
                });
    }

    @Test
    void shouldBackOffWhenSinkBeanExistsAndStatusCodeBasedWriteEnabled() {
        contextRunner
                .withBean("customSink", Sink.class, CustomSink::new)
                .withPropertyValues("logbook.write.status-code-based=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(Sink.class);
                    assertThat(context).doesNotHaveBean("statusCodeBasedEcsSink");
                    assertThat(context.getBean(Sink.class)).isExactlyInstanceOf(CustomSink.class);
                });
    }

    @Test
    void shouldBackOffWhenSinkBeanExistsAndStatusCodeBasedWriteDisabled() {
        contextRunner
                .withBean("customSink", Sink.class, CustomSink::new)
                .withPropertyValues("logbook.write.status-code-based=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(Sink.class);
                    assertThat(context).doesNotHaveBean("ecsSink");
                    assertThat(context.getBean(Sink.class)).isExactlyInstanceOf(CustomSink.class);
                });
    }

    @Test
    void shouldInitializeDefaultEcsSinkWhenStatusCodeBasedWriteDisabled() {
        contextRunner
                .withPropertyValues("logbook.write.status-code-based=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(Sink.class);
                    assertThat(context).doesNotHaveBean("statusCodeBasedEcsSink");
                    assertThat(context.getBean(Sink.class)).isExactlyInstanceOf(DefaultEcsSink.class);
                });
    }

    @Test
    void shouldInitializeDefaultEcsSinkWhenStatusCodeBasedWriteIsAbsent() {
        contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(Sink.class);
                    assertThat(context).doesNotHaveBean("statusCodeBasedEcsSink");
                    assertThat(context.getBean(Sink.class)).isExactlyInstanceOf(DefaultEcsSink.class);
                });
    }

    @Test
    void shouldInitializeStatusCodeBasedEcsSinkWhenStatusCodeBasedWriteEnabled() {
        contextRunner
                .withPropertyValues("logbook.write.status-code-based=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(Sink.class);
                    assertThat(context).doesNotHaveBean("ecsSink");
                    assertThat(context.getBean(Sink.class)).isExactlyInstanceOf(HttpStatusCodeBasedEcsSink.class);
                });
    }

    private static class CustomSink implements Sink {

        @Override
        public void write(final Precorrelation precorrelation, final HttpRequest request) throws IOException {
        }

        @Override
        public void write(final Correlation correlation, final HttpRequest request, final HttpResponse response)
                throws IOException {
        }

    }

}
