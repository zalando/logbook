package org.zalando.logbook;

import java.util.HashMap;
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
    public HttpHeaders filter(final HttpHeaders headers) {

        for (final Entry<String, Processor> entry : processors.entrySet()) {
            final String name = entry.getKey();
            final Processor processor = entry.getValue();

            if (headers.containsKey(name)) {
                return replace(headers, name, processor);
            }
        }

        return headers;
    }

    private HttpHeaders replace(
            final HttpHeaders headers,
            final String name,
            final Processor processor) {

        return headers.apply(name, values -> values.stream()
                .map(value -> replace(processor, value))
                .collect(toList()));
    }

    private String replace(final Processor processor, final String value) {
        final Matcher matcher = pattern.matcher(value);
        final StringBuffer result = new StringBuffer(value.length());
        processor.process(matcher, result);
        matcher.appendTail(result);
        return result.toString();
    }

    public void processCookie(
            final Matcher matcher, final StringBuffer result) {

        while (matcher.find()) {
            process(matcher, result);
        }
    }

    public void processSetCookie(
            final Matcher matcher, final StringBuffer result) {

        if (matcher.find()) {
            process(matcher, result);
        }
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

}
