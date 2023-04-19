package org.zalando.logbook.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.zalando.logbook.api.Logbook;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Fork(value = 1, warmups = 1)
@Warmup(iterations = 2, time = 10, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
@Measurement(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
public class LogbookBenchmark {

    @Benchmark
    public void plain(final LogbookState state, final RequestResponseState requestResponse) throws IOException {
        final Logbook logbook = state.getAutoconfigurationLogbook();

        logbook.process(requestResponse.getRequest()).write().process(requestResponse.getResponse()).write();
    }

    @Benchmark
    public void json(final LogbookState state, final RequestResponseState requestResponse) throws IOException {
        final Logbook logbook = state.getAutoconfigurationLogstashLogbook();

        logbook.process(requestResponse.getRequest()).write().process(requestResponse.getResponse()).write();
    }

    @Benchmark
    public void noop(final LogbookState state, final RequestResponseState requestResponse) throws IOException {
        final Logbook logbook = state.getNoopHttpLogFormatterLogbook();

        logbook.process(requestResponse.getRequest()).write().process(requestResponse.getResponse()).write();
    }

    public static void main(final String[] args) throws RunnerException {
        final Options options = new OptionsBuilder().include(LogbookBenchmark.class.getSimpleName())
                .forks(1).build();
        new Runner(options).run();
    }
}
