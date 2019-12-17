package org.zalando.logbook;

import java.util.ArrayList;
import java.util.List;

final class DefaultPathFilter implements PathFilter {

    private final String[] filter;
    private final String substitute;

    DefaultPathFilter(final String replacement, final String pathExpression) {
        final String[] parts = pathExpression.split("/");

        // concatenate static parts wherever possible
        final List<String> filterImpl = new ArrayList<>();

        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            final String part = parts[i];
            if (part.startsWith("{") && part.endsWith("}")) {
                if (builder.length() > 0) {
                    filterImpl.add(builder.toString());

                    builder.setLength(0);
                }
                filterImpl.add(null);
            } else {
                builder.append(part);
            }
            if (i + 1 < parts.length) {
                builder.append('/');
            }

        }
        if (builder.length() > 0) {
            filterImpl.add(builder.toString());
        }

        final StringBuilder substituteBuilder = new StringBuilder();

        for (final String filter : filterImpl) {
            if (filter == null) {
                substituteBuilder.append(replacement);
            } else {
                substituteBuilder.append(filter);
            }
        }
        this.filter = filterImpl.toArray(new String[0]);
        this.substitute = substituteBuilder.toString();
    }

    @Override
    public String filter(final String path) {
        // this approach avoids creating new objects if
        // - the filter does not match
        // - the filter matches exactly
        int filterIndex = 0;
        int previousIndex = 0;
        do {
            if (filter[filterIndex] != null) {
                // path must match on string
                if (!path.regionMatches(previousIndex, filter[filterIndex], 0, filter[filterIndex].length())) {
                    return path;
                }
                previousIndex += filter[filterIndex].length();
                filterIndex++;
            } else {
                // locate next slash
                final int nextIndex = path.indexOf('/', previousIndex);
                if (nextIndex != -1) {
                    previousIndex = nextIndex;
                } else {
                    previousIndex = path.length();

                    break;
                }
                filterIndex++;
            }
        } while (filterIndex < filter.length);

        if (previousIndex == path.length()) {
            return substitute;
        }
        return substitute + path.substring(previousIndex);
    }

}
