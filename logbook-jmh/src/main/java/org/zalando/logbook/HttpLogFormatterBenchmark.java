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

import java.util.concurrent.TimeUnit;

@Fork(value = 1, warmups = 1)
@Warmup(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
@Measurement(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
public class HttpLogFormatterBenchmark {

    @Benchmark
    public Object jsonRequest(final RequestResponseState state, final HttpLogFormatterState httpLogFormatterState) throws Exception {
        return httpLogFormatterState.getJsonHttpLogFormatter().format(state.getDefaultPrecorrelation(), state.getRequest());
    }
    
    @Benchmark
    public Object jsonResponse(final RequestResponseState state, final HttpLogFormatterState httpLogFormatterState) throws Exception {
        return httpLogFormatterState.getJsonHttpLogFormatter().format(state.getDefaultCorrelation(), state.getResponse());
    }

    @Benchmark
    public Object fastJsonRequest(final RequestResponseState state, final HttpLogFormatterState httpLogFormatterState) throws Exception {
        return httpLogFormatterState.getFastJsonHttpLogFormatter().format(state.getDefaultPrecorrelation(), state.getRequest());
    }

    @Benchmark
    public Object fastJsonResponse(final RequestResponseState state, final HttpLogFormatterState httpLogFormatterState) throws Exception {
        return httpLogFormatterState.getFastJsonHttpLogFormatter().format(state.getDefaultCorrelation(), state.getResponse());
    }
    
    @Benchmark
    public Object defaultRequest(final RequestResponseState state, final HttpLogFormatterState httpLogFormatterState) throws Exception {
        return httpLogFormatterState.getDefaultHttpLogFormatter().format(state.getDefaultPrecorrelation(), state.getRequest());
    }
    
    @Benchmark
    public Object defaultResponse(final RequestResponseState state, final HttpLogFormatterState httpLogFormatterState) throws Exception {
        return httpLogFormatterState.getDefaultHttpLogFormatter().format(state.getDefaultCorrelation(), state.getResponse());
    }    

    public static void main(final String[] args) throws RunnerException {
        final Options options = new OptionsBuilder().include(HttpLogFormatterBenchmark.class.getSimpleName())
                .forks(1).build();
        new Runner(options).run();
    }

}
