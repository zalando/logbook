package org.zalando.logbook;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

final class GlobTest {

    static List<Arguments> allows() {
        return Arrays.asList(
                Arguments.of("**", "/a"),
                Arguments.of("/**", "/a"),
                Arguments.of("/a", "/a"),
                Arguments.of("/?", "/a"),
                Arguments.of("/a/**", "/a/"),
                Arguments.of("/a/**", "/a"),
                Arguments.of("/a/*", "/a/b"),
                Arguments.of("/a/**", "/a/b/c"),
                Arguments.of("/a/*/c", "/a/b/c"),
                Arguments.of("/a/**", "/a/b/c"),
                Arguments.of("/a/*/c/**", "/a/b/c"),
                Arguments.of("/a/?/c/**", "/a/b/c"),

                // test exact matching
                Arguments.of("test", "test"),
                Arguments.of("/test", "/test"),
                Arguments.of("http://example.org", "http://example.org"),

                // test matching with ?'s
                Arguments.of("t?st", "test"),
                Arguments.of("??st", "test"),
                Arguments.of("tes?", "test"),
                Arguments.of("te??", "test"),
                Arguments.of("?es?", "test"),

                Arguments.of("*", "test"),
                Arguments.of("test*", "test"),
                Arguments.of("test*", "testTest"),
                Arguments.of("test/*", "test/Test"),
                Arguments.of("test/*", "test/t"),
                Arguments.of("test/*", "test/"),
                Arguments.of("*test*", "AnothertestTest"),
                Arguments.of("*test", "Anothertest"),
                Arguments.of("*.*", "test."),
                Arguments.of("*.*", "test.test"),
                Arguments.of("*.*", "test.test.test"),
                Arguments.of("test*aaa", "testblaaaa"),

                // test matching with ?'s and /'s
                Arguments.of("/?", "/a"),
                Arguments.of("/?/a", "/a/a"),
                Arguments.of("/a/?", "/a/b"),
                Arguments.of("/??/a", "/aa/a"),
                Arguments.of("/a/??", "/a/bb"),
                Arguments.of("/?", "/a"),

                // test matching with **'s
                Arguments.of("/**", "/testing/testing"),
                Arguments.of("/*/**", "/testing/testing"),
                Arguments.of("/**/*", "/testing/testing"),
                Arguments.of("/bla/**/bla", "/bla/testing/testing/bla"),
                Arguments.of("/bla/**/bla", "/bla/testing/testing/bla/bla"),
                Arguments.of("/**/test", "/bla/bla/test"),
                Arguments.of("/bla/**/**/bla", "/bla/bla/bla/bla/bla/bla"),
                Arguments.of("/bla*bla/test", "/blaXXXbla/test"),
                Arguments.of("/*bla/test", "/XXXbla/test"),

                Arguments.of("/*bla*/**/bla/**", "/XXXblaXXXX/testing/testing/bla/testing/testing/"),
                Arguments.of("/*bla*/**/bla/*", "/XXXblaXXXX/testing/testing/bla/testing"),
                Arguments.of("/*bla*/**/bla/**", "/XXXblaXXXX/testing/testing/bla/testing/testing"),
                Arguments.of("/*bla*/**/bla/**", "/XXXblaXXXX/testing/testing/bla/testing/testing.jpg"),

                Arguments.of("*bla*/**/bla/**", "XXXblaXXXX/testing/testing/bla/testing/testing/"),
                Arguments.of("*bla*/**/bla/*", "XXXblaXXXX/testing/testing/bla/testing"),
                Arguments.of("*bla*/**/bla/**", "XXXblaXXXX/testing/testing/bla/testing/testing"),

                Arguments.of("/foo/bar/**", "/foo/bar"),

                Arguments.of("", "")
        );
    }

    static List<Arguments> denies() {
        return Arrays.asList(
                Arguments.of("/a", "/abc"),
                Arguments.of("a/**/b", "/a"),
                Arguments.of("/a/**/b", "a"),
                Arguments.of("/?", "/abc"),
                Arguments.of("/a/**", "/b"),
                Arguments.of("/a/**", "/ab"),
                Arguments.of("/test.jpg", "test.jpg"),
                Arguments.of("test", "/test"),
                Arguments.of("/test", "test"),

                // test matching with ?'s
                Arguments.of("tes?", "tes"),
                Arguments.of("tes?", "testt"),
                Arguments.of("tes?", "tsst"),

                // test matching with *'s
                Arguments.of("test*", "tst"),
                Arguments.of("test*", "tsttest"),
                Arguments.of("test*", "test/"),
                Arguments.of("test*", "test/t"),
                Arguments.of("test/*", "test"),
                Arguments.of("*test*", "tsttst"),
                Arguments.of("*test", "tsttst"),
                Arguments.of("*.*", "tsttst"),
                Arguments.of("test*aaa", "test"),
                Arguments.of("test*aaa", "testblaaab"),

                // test matching with **'s
                Arguments.of("/bla*bla/test", "/blaXXXbl/test"),
                Arguments.of("/*bla/test", "XXXblab/test"),
                Arguments.of("/*bla/test", "XXXbl/test"),

                Arguments.of("/????", "/bala/bla"),
                Arguments.of("/**/*bla", "/bla/bla/bla/bbb"),
                Arguments.of("*bla*/**/bla/*", "XXXblaXXXX/testing/testing/bla/testing/testing"),

                Arguments.of("/x/x/**/bla", "/x/x/x/")
        );
    }

    @ParameterizedTest
    @MethodSource("allows")
    void allow(final String pattern, final String uri) {
        assertThat(pattern + " doesn't match " + uri, Glob.compile(pattern).test(uri), is(true));
    }

    @ParameterizedTest
    @MethodSource("denies")
    void deny(final String pattern, final String uri) {
        assertThat(pattern + " matches " + uri + " but shouldn't", Glob.compile(pattern).test(uri), is(false));
    }

}
