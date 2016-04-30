package org.zalando.logbook;

/*
 * #%L
 * Logbook: Core
 * %%
 * Copyright (C) 2015 - 2016 Zalando SE
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.base.Splitter;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Splitter.on;

final class Glob {

    public static Predicate<String> compile(final String pattern) {
        final boolean q = pattern.indexOf('?') == -1;
        final int asterisk = pattern.indexOf('*');

        if (q && asterisk == -1) {
            return pattern::equals;
        } else if (pattern.equals("/**") || pattern.equals("**")) {
            return $ -> true;
        } else if (pattern.endsWith("/**")
                && q
                && asterisk == pattern.length() - 2) {
            return new SuffixGlob(pattern.substring(0, pattern.length() - 3));
        } else {
            return new DefaultGlob(pattern);
        }
    }

    private static class SuffixGlob implements Predicate<String> {

        private final String subPath;

        private SuffixGlob(final String subPath) {
            this.subPath = subPath;
        }

        @Override
        public boolean test(final String path) {
            return path.startsWith(subPath)
                    && (path.length() == subPath.length() || path.charAt(subPath.length()) == '/');
        }

    }

    private static final class DefaultGlob implements Predicate<String> {

        private static final Pattern GLOB_PATTERN = Pattern.compile("\\?|\\*");
        private static final String DEFAULT_VARIABLE_PATTERN = "(.*)";
        private static final Splitter SLASH = on('/').trimResults().omitEmptyStrings();

        private final String pattern;

        DefaultGlob(final String pattern) {
            this.pattern = pattern;
        }

        @Override
        public boolean test(final String path) {
            if (path.startsWith("/") != pattern.startsWith("/")) {
                return false;
            }

            final String[] patternSegments = tokenizePath(pattern);
            final String[] pathSegments = tokenizePath(path);

            int patternSegmentIndex = 0;
            int patternSegmentEnd = patternSegments.length - 1;
            int pathSegmentIndex = 0;
            int pathIndexEnd = pathSegments.length - 1;

            while (patternSegmentIndex <= patternSegmentEnd && pathSegmentIndex <= pathIndexEnd) {
                final String patternSegment = patternSegments[patternSegmentIndex];
                if ("**".equals(patternSegment)) {
                    break;
                }
                if (!match(patternSegment, pathSegments[pathSegmentIndex])) {
                    return false;
                }
                patternSegmentIndex++;
                pathSegmentIndex++;
            }

            if (pathSegmentIndex > pathIndexEnd) {
                if (patternSegmentIndex > patternSegmentEnd) {
                    return pattern.endsWith("/") == path.endsWith("/");
                }
                if (patternSegmentIndex == patternSegmentEnd && patternSegments[patternSegmentIndex].equals("*") && path.endsWith("/")) {
                    return true;
                }
                for (int i = patternSegmentIndex; i <= patternSegmentEnd; i++) {
                    if (!patternSegments[i].equals("**")) {
                        return false;
                    }
                }
                return true;
            } else if (patternSegmentIndex > patternSegmentEnd) {
                return false;
            }

            while (patternSegmentIndex <= patternSegmentEnd && pathSegmentIndex <= pathIndexEnd) {
                final String patternSegment = patternSegments[patternSegmentEnd];
                if (patternSegment.equals("**")) {
                    break;
                }
                if (!match(patternSegment, pathSegments[pathIndexEnd])) {
                    return false;
                }
                patternSegmentEnd--;
                pathIndexEnd--;
            }
            if (pathSegmentIndex > pathIndexEnd) {
                for (int i = patternSegmentIndex; i <= patternSegmentEnd; i++) {
                    if (!patternSegments[i].equals("**")) {
                        return false;
                    }
                }
                return true;
            }

            while (patternSegmentIndex != patternSegmentEnd && pathSegmentIndex <= pathIndexEnd) {
                int temporaryPatternIndex = -1;
                for (int i = patternSegmentIndex + 1; i <= patternSegmentEnd; i++) {
                    if (patternSegments[i].equals("**")) {
                        temporaryPatternIndex = i;
                        break;
                    }
                }
                if (temporaryPatternIndex == patternSegmentIndex + 1) {
                    patternSegmentIndex++;
                    continue;
                }
                final int patternLength = (temporaryPatternIndex - patternSegmentIndex - 1);
                final int strLength = (pathIndexEnd - pathSegmentIndex + 1);
                int foundSegment = -1;

                loop:
                for (int i = 0; i <= strLength - patternLength; i++) {
                    for (int j = 0; j < patternLength; j++) {
                        final String subPat = patternSegments[patternSegmentIndex + j + 1];
                        final String subStr = pathSegments[pathSegmentIndex + i + j];
                        if (!match(subPat, subStr)) {
                            continue loop;
                        }
                    }
                    foundSegment = pathSegmentIndex + i;
                    break;
                }

                if (foundSegment == -1) {
                    return false;
                }

                patternSegmentIndex = temporaryPatternIndex;
                pathSegmentIndex = foundSegment + patternLength;
            }

            for (int i = patternSegmentIndex; i <= patternSegmentEnd; i++) {
                if (!patternSegments[i].equals("**")) {
                    return false;
                }
            }

            return true;
        }

        private String[] tokenizePath(final String path) {
            final List<String> tokens = SLASH.splitToList(path);
            return tokens.toArray(new String[tokens.size()]);
        }

        private boolean match(final String pattern, final String str) {
            return toPattern(pattern).matcher(str).matches();
        }

        private Pattern toPattern(final String pattern) {
            final StringBuilder result = new StringBuilder();
            final Matcher matcher = GLOB_PATTERN.matcher(pattern);
            int end = 0;
            while (matcher.find()) {
                result.append(quote(pattern, end, matcher.start()));
                final String match = matcher.group();
                if ("?".equals(match)) {
                    result.append('.');
                } else if ("*".equals(match)) {
                    result.append(".*");
                } else {
                    result.append(DEFAULT_VARIABLE_PATTERN);
                }
                end = matcher.end();
            }
            result.append(quote(pattern, end, pattern.length()));
            return Pattern.compile(result.toString());
        }

        private String quote(final String s, final int start, final int end) {
            if (start == end) {
                return "";
            }
            return Pattern.quote(s.substring(start, end));
        }

    }

}
