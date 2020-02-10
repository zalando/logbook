package org.zalando.logbook;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toList;

final class CookieHeaderFilter implements HeaderFilter {

    private final Pattern pattern = compile("(?<name>[^=; ]+)=(?<value>[^; ]*)");

    private final Map<String, Processor> processors = new HashMap<>();
    private final Predicate<String> predicate;
    private final String replacement;

    @FunctionalInterface
    private interface Processor {
        void process(Matcher matcher, StringBuffer result);
    }

    public CookieHeaderFilter(
            final Predicate<String> predicate, final String replacement) {
        this.predicate = predicate;
        this.replacement = replacement;

        this.processors.put("Cookie", this::processCookie);
        this.processors.put("Set-Cookie", this::processSetCookie);
    }

    @Override
    public Map<String, List<String>> filter(
            final Map<String, List<String>> headers) {

        for (final Entry<String, Processor> entry : processors.entrySet()) {
            final String header = entry.getKey();
            final Processor processor = entry.getValue();

            if (headers.containsKey(header)) {
                return replace(headers, header, processor);
            }
        }

        return headers;
    }

    public void processCookie(final Matcher matcher, final StringBuffer result) {
        while (matcher.find()) {
            process(matcher, result);
        }
    }

    public void processSetCookie(final Matcher matcher, final StringBuffer result) {
        if (matcher.find()) {
            process(matcher, result);
        }
    }

    private Map<String, List<String>> replace(
            final Map<String, List<String>> headers,
            final String key,
            final Processor processor) {

        final List<String> values = headers.get(key);

        final List<String> cookies = values.stream()
                .map(value -> {
                    final Matcher matcher = pattern.matcher(value);
                    final StringBuffer result = new StringBuffer(value.length());
                    processor.process(matcher, result);
                    matcher.appendTail(result);
                    return result.toString();
                })
                .collect(toList());

        return replace(headers, key, cookies);
    }

    private void process(final Matcher matcher, final StringBuffer result) {
        if (predicate.test(matcher.group("name"))) {
            matcher.appendReplacement(result, "${name}");
            result.append('=');
            result.append(replacement);
        } else {
            matcher.appendReplacement(result, "$0");
        }
    }

    private Map<String, List<String>> replace(
            final Map<String, List<String>> headers, final String name, final List<String> value) {
        final Map<String, List<String>> result = Headers.empty();
        result.putAll(headers);
        result.put(name, value);
        return result;
    }

}
