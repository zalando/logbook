package org.zalando.logbook;

import java.util.concurrent.TimeUnit;

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

@Fork(value = 1, warmups = 1)
@Warmup(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
@Measurement(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
public class HttpLogFormatterBenchmark {

    @Benchmark
    public Object jsonRequest(RequestResponseState state, HttpLogFormatterState httpLogFormatterState) throws Exception {
        return httpLogFormatterState.getJsonHttpLogFormatter().format(state.getDefaultPrecorrelation(), state.getRequest());
    }
    
    @Benchmark
    public Object jsonResponse(RequestResponseState state, HttpLogFormatterState httpLogFormatterState) throws Exception {
        return httpLogFormatterState.getJsonHttpLogFormatter().format(state.getDefaultCorrelation(), state.getResponse());
    }
    
    @Benchmark
    public Object defaultRequest(RequestResponseState state, HttpLogFormatterState httpLogFormatterState) throws Exception {
        return httpLogFormatterState.getDefaultHttpLogFormatter().format(state.getDefaultPrecorrelation(), state.getRequest());
    }
    
    @Benchmark
    public Object defaultResponse(RequestResponseState state, HttpLogFormatterState httpLogFormatterState) throws Exception {
        return httpLogFormatterState.getDefaultHttpLogFormatter().format(state.getDefaultCorrelation(), state.getResponse());
    }    

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder().include(HttpLogFormatterBenchmark.class.getSimpleName())
                .forks(1).build();
        new Runner(options).run();
    }

}
