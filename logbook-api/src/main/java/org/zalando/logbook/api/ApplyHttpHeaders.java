package org.zalando.logbook.api;

import com.google.gag.annotation.remark.ThisWouldBeOneLineIn;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.UnaryOperator;

import static java.util.Collections.singleton;
import static org.zalando.logbook.api.Fold.fold;

interface ApplyHttpHeaders extends HttpHeaders {

    @Override
    default HttpHeaders apply(
            final String name,
            final UnaryOperator<List<String>> operator) {
        return apply(singleton(name), (ignored, previous) ->
                operator.apply(previous));
    }

    @Override
    default HttpHeaders apply(
            final Collection<String> names,
            final BiFunction<String, List<String>, Collection<String>> operator) {

        final HttpHeaders self = this;
        return fold(names, self, (result, name) -> {
            final List<String> previous = get(name);
            return applyTo(operator, name, previous, result);
        });
    }

    @Override
    default HttpHeaders apply(
            final BiPredicate<String, List<String>> predicate,
            final BiFunction<String, List<String>, Collection<String>> operator) {

        return apply((name, previous) -> {
            if (predicate.test(name, previous)) {
                return operator.apply(name, previous);
            }
            return previous;
        });
    }

    @Override
    default HttpHeaders apply(
            final BiFunction<String, List<String>, Collection<String>> operator) {

        final HttpHeaders self = this;
        return fold(entrySet(), self, (result, entry) -> {
            final String name = entry.getKey();
            final List<String> previous = entry.getValue();
            return applyTo(operator, name, previous, result);
        });
    }

    // Effectively package-private because this interface is and so are all
    // implementations of it. Ideally it would be private
    @ThisWouldBeOneLineIn(language = "Java 9", toWit = "private")
    default HttpHeaders applyTo(
            final BiFunction<String, List<String>, Collection<String>> operator,
            final String name,
            final List<String> previous,
            final HttpHeaders headers) {

        @Nullable final Collection<String> next =
                operator.apply(name, previous);

        return next == null ?
                headers.delete(name) :
                headers.update(name, next);
    }

}
