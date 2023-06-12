package org.zalando.logbook.core;

import org.zalando.logbook.PathFilter;

import java.util.Objects;
import java.util.function.UnaryOperator;

final class DynamicPathFilter implements PathFilter {

    private final UnaryOperator<String> replacementFunction;
    private final String[] parts;

    DynamicPathFilter(final UnaryOperator<String> replacementFunction, final String pathExpression) {
        this.replacementFunction = replacementFunction;

        final String[] parts = pathExpression.split("/");

        for (int i = 0; i < parts.length; i++) {
            final String part = parts[i];
            if (part.startsWith("{") && part.endsWith("}")) {
                parts[i] = null;
            }
        }

        this.parts = parts;
    }


    @Override
    public String filter(final String path) {
        final String[] pathParts = path.split("/");

        if (parts.length > pathParts.length) {
            return path;
        }

        final StringBuilder builder = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (Objects.isNull(parts[i])) {
                if (i > 0) {
                    builder.append('/');
                }
                builder.append(replacementFunction.apply(pathParts[i]));
            } else {
                if (Objects.equals(parts[i], pathParts[i])) {
                    if (!parts[i].isEmpty()) {
                        builder.append('/');
                    }
                    builder.append(pathParts[i]);
                } else {
                    return path;
                }
            }
        }

        if (parts.length < pathParts.length) {
            for (int i = parts.length; i < pathParts.length; i++) {
                builder.append('/');
                builder.append(pathParts[i]);
            }
        }

        return builder.toString();
    }
}
