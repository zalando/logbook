package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public final class GlobTest {

    @Test
    void shouldMatchAllMatch() {
        allow("**", "/a");
        allow("/**", "/a");
        allow("/a", "/a");
        deny("/a", "/abc");
        deny("a/**/b", "/a");
        deny("/a/**/b", "a");
        allow("/?", "/a");
        deny("/?", "/abc");
        allow("/a/**", "/a/");
        deny("/a/**", "/b");
        allow("/a/**", "/a");
        deny("/a/**", "/ab");
        allow("/a/*", "/a/b");
        allow("/a/**", "/a/b/c");
        allow("/a/*/c", "/a/b/c");
        allow("/a/**", "/a/b/c");
        allow("/a/*/c/**", "/a/b/c");
        allow("/a/?/c/**", "/a/b/c");

        // test exact matching
        allow("test", "test");
        allow("/test", "/test");
        allow("http://example.org", "http://example.org");
        deny("/test.jpg", "test.jpg");
        deny("test", "/test");
        deny("/test", "test");

        // test matching with ?'s
        allow("t?st", "test");
        allow("??st", "test");
        allow("tes?", "test");
        allow("te??", "test");
        allow("?es?", "test");
        deny("tes?", "tes");
        deny("tes?", "testt");
        deny("tes?", "tsst");

        // test matching with *'s
        allow("*", "test");
        allow("test*", "test");
        allow("test*", "testTest");
        allow("test/*", "test/Test");
        allow("test/*", "test/t");
        allow("test/*", "test/");
        allow("*test*", "AnothertestTest");
        allow("*test", "Anothertest");
        allow("*.*", "test.");
        allow("*.*", "test.test");
        allow("*.*", "test.test.test");
        allow("test*aaa", "testblaaaa");
        deny("test*", "tst");
        deny("test*", "tsttest");
        deny("test*", "test/");
        deny("test*", "test/t");
        deny("test/*", "test");
        deny("*test*", "tsttst");
        deny("*test", "tsttst");
        deny("*.*", "tsttst");
        deny("test*aaa", "test");
        deny("test*aaa", "testblaaab");

        // test matching with ?'s and /'s
        allow("/?", "/a");
        allow("/?/a", "/a/a");
        allow("/a/?", "/a/b");
        allow("/??/a", "/aa/a");
        allow("/a/??", "/a/bb");
        allow("/?", "/a");

        // test matching with **'s
        allow("/**", "/testing/testing");
        allow("/*/**", "/testing/testing");
        allow("/**/*", "/testing/testing");
        allow("/bla/**/bla", "/bla/testing/testing/bla");
        allow("/bla/**/bla", "/bla/testing/testing/bla/bla");
        allow("/**/test", "/bla/bla/test");
        allow("/bla/**/**/bla", "/bla/bla/bla/bla/bla/bla");
        allow("/bla*bla/test", "/blaXXXbla/test");
        allow("/*bla/test", "/XXXbla/test");
        deny("/bla*bla/test", "/blaXXXbl/test");
        deny("/*bla/test", "XXXblab/test");
        deny("/*bla/test", "XXXbl/test");

        deny("/????", "/bala/bla");
        deny("/**/*bla", "/bla/bla/bla/bbb");

        allow("/*bla*/**/bla/**", "/XXXblaXXXX/testing/testing/bla/testing/testing/");
        allow("/*bla*/**/bla/*", "/XXXblaXXXX/testing/testing/bla/testing");
        allow("/*bla*/**/bla/**", "/XXXblaXXXX/testing/testing/bla/testing/testing");
        allow("/*bla*/**/bla/**", "/XXXblaXXXX/testing/testing/bla/testing/testing.jpg");

        allow("*bla*/**/bla/**", "XXXblaXXXX/testing/testing/bla/testing/testing/");
        allow("*bla*/**/bla/*", "XXXblaXXXX/testing/testing/bla/testing");
        allow("*bla*/**/bla/**", "XXXblaXXXX/testing/testing/bla/testing/testing");
        deny("*bla*/**/bla/*", "XXXblaXXXX/testing/testing/bla/testing/testing");

        deny("/x/x/**/bla", "/x/x/x/");

        allow("/foo/bar/**", "/foo/bar");

        allow("", "");
    }

    private void allow(final String pattern, final String uri) {
        assertThat(pattern + " doesn't match " + uri, Glob.compile(pattern).test(uri), is(true));
    }

    private void deny(final String pattern, final String uri) {
        assertThat(pattern + " matches " + uri + " but shouldn't", Glob.compile(pattern).test(uri), is(false));
    }

}
