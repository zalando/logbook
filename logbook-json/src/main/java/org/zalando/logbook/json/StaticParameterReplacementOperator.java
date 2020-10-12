package org.zalando.logbook.json;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.function.BinaryOperator;

import static org.zalando.logbook.json.PrimitiveJsonPropertyBodyFilter.quote;

@AllArgsConstructor
@EqualsAndHashCode
public class StaticParameterReplacementOperator<T> implements BinaryOperator<String> {
    private final T replacement;

    private final boolean addQuotation;

    @Override
    public String apply(String s, String s2) {
        return addQuotation ? quote(replacement.toString()) : replacement.toString();
    }
}
