package org.zalando.logbook.json;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.function.BinaryOperator;

@AllArgsConstructor
@EqualsAndHashCode
final class StaticParameterReplacementOperator<T> implements BinaryOperator<String> {
    private final T replacement;

    @Override
    public String apply(String s, String s2) {
        return replacement.toString();
    }
}
