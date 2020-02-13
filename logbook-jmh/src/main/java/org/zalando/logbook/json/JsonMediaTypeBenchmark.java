package org.zalando.logbook.json;

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
@Warmup(iterations = 2, time = 3, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
public class JsonMediaTypeBenchmark {

    @Benchmark
    public Object contentType1() throws IOException {
        return JsonMediaType.JSON.test("application/json");
    }

    @Benchmark
    public Object contentType2() throws IOException {
        return JsonMediaType.JSON.test("application/json;charset=utf-8");
    }

    @Benchmark
    public Object contentType3() throws IOException {
        return JsonMediaType.JSON.test("application/abc+json");
    }

    @Benchmark
    public Object contentType4() throws IOException {
        return JsonMediaType.JSON.test("application/abc+json;charset=utf-8");
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder().include(JsonMediaTypeBenchmark.class.getSimpleName())
                .forks(1).build();
        new Runner(options).run();
    }    
}
