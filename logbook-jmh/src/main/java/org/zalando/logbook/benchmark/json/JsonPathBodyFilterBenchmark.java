package org.zalando.logbook.benchmark.json;

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
import org.zalando.logbook.json.JsonBodyFilters;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static org.zalando.logbook.json.JsonPathBodyFilters.jsonPath;

@Fork(value = 1, warmups = 1)
@Warmup(iterations = 2, time = 3, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
public class JsonPathBodyFilterBenchmark {

    public static final String BODY = "{\"test\": \"5213486633218931\", \"test123\": \"5213486633218931\"}";
    public static final String CONTENT_TYPE = "application/json";

    @Benchmark
    public void replaceStringDynamicallyBenchmark() {
        jsonPath("$.test").replace(Pattern.compile("(\\d{6})\\d+(\\d{4})"), "$1********$2")
                .filter(CONTENT_TYPE, BODY);
    }

    @Benchmark
    public void replaceStringJsonPathBenchmark() {
        jsonPath("$.test").replace("***")
                .filter(CONTENT_TYPE, BODY);
    }

    @Benchmark
    public void replaceStringPrimitiveBenchmark() {
        JsonBodyFilters.replaceJsonStringProperty(s -> s.equals("test"), "***").filter(CONTENT_TYPE, BODY);
    }

    public static void main(final String[] args) throws RunnerException {
        final Options options = new OptionsBuilder().include(JsonPathBodyFilterBenchmark.class.getSimpleName())
                .forks(1).build();
        new Runner(options).run();
    }


}
