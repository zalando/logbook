# logbook-jmh
Module for performance-testing using the [JMH](https://openjdk.java.net/projects/code-tools/jmh/) framework.

## Introduction
Performance analysis is complicated due to the dynamic nature of the JVM Hotspot implementation. 

For example the JVM Hotspot is affected by the classes it loads and better optimize when there is only one implementation of an interface in use. 

In practice much of the dynamic nature can be handled by using so-called forks and warmups:

 * forks makes sure the individual benchmarks run alone
 * warmups makes sure optimizations by the JVM Hotspot are finished before the measurements are taken

## Getting started
Get familiar with JMH:

 * [baeldung](https://www.baeldung.com/java-microbenchmark-harness)
 * [jenkov](http://tutorials.jenkov.com/java-performance/jmh.html)
 
Download a profiler like [VisualVM](https://visualvm.github.io/) for drilling down to method level during development.

In this project, there is end-to-end tests in the [LogbookBenchmark](src/main/java/org/zalando/logbook/LogbookBenchmark.java) which touches the most commonly used code paths.

Execute `LogbookBenchmark` using the command

```
mvn clean package && java -jar target/benchmark.jar LogbookBenchmark -rf json
```

and view the resulting `jmh-result.json` by dropping the file into a [visualizer](https://jmh.morethan.io).

Remember to disable CPU turbo / boost in BIOS, and close active programs competing for CPU resources.

### Performance analysis
Analyze the source code, tweak the end-to-end and noop benchmarks and/or use a profiler to identify hotspots. 

Benchmarks can be executed as standalone programs (using `main(..)` method) directly from your IDE. This is less accurate than running from the command line, but convenient during active development, especially for drilling down using a profiler like [VisualVM](https://visualvm.github.io/) or such. 

## Writing a benchmark
Once a potential hotspot is identified, capture the initial state by writing a baseline benchmark. If missing, add unit tests, so you're sure to be comparing apples to apples. Also add a (as close as possible) no-operation / pass-through benchmark to sanity-check the upper limit on your results. Please note that this will need to be submitted in its own PR.

Then add alternative implementations and their corresponding benchmarks. The benchmarks you want to compare go into the same class file (so that the visualizer presents them together). 
