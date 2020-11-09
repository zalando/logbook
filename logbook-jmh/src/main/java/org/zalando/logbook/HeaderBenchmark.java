package org.zalando.logbook;

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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Fork(value = 1, warmups = 1)
@Warmup(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
@Measurement(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
public class HeaderBenchmark {

    @Benchmark
    public void autoconfigurationRequest(final HeaderState headerState) throws IOException {
        headerState.getAutoconfigurationFilter().filter(headerState.getAllRequestHeaders());
    }

    @Benchmark
    public void autoconfigurationResponse(final HeaderState headerState) throws IOException {
        headerState.getAutoconfigurationFilter().filter(headerState.getAllResponseHeaders());
    }

    @Benchmark
    public void replace1xRequest(final HeaderState headerState) throws IOException {
        headerState.getReplaceFilter().filter(headerState.getAllRequestHeaders());
    }

    @Benchmark
    public void replace1xResponse(final HeaderState headerState) throws IOException {
        headerState.getReplaceFilter().filter(headerState.getAllResponseHeaders());
    }

    @Benchmark
    public void replace2xRequest(final HeaderState headerState) throws IOException {
        headerState.getReplace2xFilter().filter(headerState.getAllRequestHeaders());
    }

    @Benchmark
    public void replace2xResponse(final HeaderState headerState) throws IOException {
        headerState.getReplace2xFilter().filter(headerState.getAllResponseHeaders());
    }

    @Benchmark
    public void replace2xResponseShopify(final HeaderState headerState) throws IOException {
        headerState.getReplace2xFilter().filter(headerState.getShopifyResponseHeaders());
    }

    public static void main(final String[] args) throws RunnerException {
        final Options options = new OptionsBuilder().include(HeaderBenchmark.class.getSimpleName())
                .forks(1).build();
        new Runner(options).run();
    }
}
