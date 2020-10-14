package org.zalando.logbook.json;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

import static org.zalando.logbook.json.PrimitiveJsonPropertyBodyFilter.quote;

@AllArgsConstructor
@EqualsAndHashCode
final class StringFunctionReplacementOperator implements BinaryOperator<String> {
    private final BiFunction<String, String, String> stringReplacementFunction;

    @Override
    public String apply(String s, String s2) {
        return quote(stringReplacementFunction.apply(s, s2));
    }
}
