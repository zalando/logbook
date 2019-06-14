package org.zalando.logbook;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

public class DefaultPathFilterTest {

    @Test
    public void testFilterOneReplacement() {
        String path = "/profiles/123456789/user.json";
        String expr = "/profiles/{organization}/user.json";

        PathFilter regexpPathUriFilter = new DefaultPathFilter("XXX", expr);

        String result = regexpPathUriFilter.filter(path);
        assertThat(result, is("/profiles/XXX/user.json"));
    }

    @Test
    public void testFilterTwoSideBySideReplacements() {
        String path = "/profiles/123456789/test@test.com/user.json";
        String expr = "/profiles/{organization}/{email}/user.json";

        PathFilter regexpPathUriFilter = new DefaultPathFilter("XXX", expr);

        String result = regexpPathUriFilter.filter(path);
        assertThat(result, is("/profiles/XXX/XXX/user.json"));
    }

    @Test
    public void testFilterTwoReplacements() {
        String path = "/profiles/123456789/my/test@test.com/user.json";
        String expr = "/profiles/{organization}/my/{email}/user.json";

        PathFilter regexpPathUriFilter = new DefaultPathFilter("XXX", expr);

        String result = regexpPathUriFilter.filter(path);
        assertThat(result, is("/profiles/XXX/my/XXX/user.json"));
    }

    @Test
    public void testFilterSubpath() {
        String path = "/profiles/123456789/email/test@test.com/user/extra";
        String expr = "/profiles/{organization}/email/{email}/user";

        PathFilter regexpPathUriFilter = new DefaultPathFilter("XXX", expr);

        String result = regexpPathUriFilter.filter(path);
        assertThat(result, is("/profiles/XXX/email/XXX/user/extra"));
    }

    @Test
    public void testFilterStart() {
        String path = "/a/b/c/d/e";
        String expr = "/{a}/b/c/d/e";

        PathFilter regexpPathUriFilter = new DefaultPathFilter("XXX", expr);

        String result = regexpPathUriFilter.filter(path);
        assertThat(result, is("/XXX/b/c/d/e"));
    }
    @Test
    public void testFilterStartNoSlash() {
        String path = "a/b/c/d/e";
        String expr = "{a}/b/c/d/e";

        PathFilter regexpPathUriFilter = new DefaultPathFilter("XXX", expr);

        String result = regexpPathUriFilter.filter(path);
        assertThat(result, is("XXX/b/c/d/e"));
    }

    @Test
    public void testFilterEnd() {
        String path = "/a/b/c/d/e";
        String expr = "/a/b/c/d/{e}";

        PathFilter regexpPathUriFilter = new DefaultPathFilter("XXX", expr);

        String result = regexpPathUriFilter.filter(path);
        assertThat(result, is("/a/b/c/d/XXX"));
    }

    @Test
    public void testReturnsSameInstance() {
        String path = "/a/b/c/d/e";
        String expr = "/a/b/{c}/d/e";

        PathFilter regexpPathUriFilter = new DefaultPathFilter("XXX", expr);

        String result1 = regexpPathUriFilter.filter(path);
        String result2 = regexpPathUriFilter.filter(path);
        assertThat(result1, sameInstance(result2));
    }

    @Test
    public void testReturnsInputInstance() {
        String path = "/b/c/d/e/f";
        String expr = "/a/b/{c}/d/e";

        PathFilter regexpPathUriFilter = new DefaultPathFilter("XXX", expr);

        String result = regexpPathUriFilter.filter(path);
        assertThat(result, sameInstance(path));
    }

    @Test
    public void testIgnoresIncorrectExpression1() {
        String path = "/a/b/c/d/e";
        String expr = "/a/b/{c/d/e";

        PathFilter regexpPathUriFilter = new DefaultPathFilter("XXX", expr);

        String result = regexpPathUriFilter.filter(path);
        assertThat(path, sameInstance(result));
    }

    @Test
    public void testIgnoresIncorrectExpression2() {
        String path = "/a/b/c/d/e";
        String expr = "/a/b/c}/d/e";

        PathFilter regexpPathUriFilter = new DefaultPathFilter("XXX", expr);

        String result = regexpPathUriFilter.filter(path);
        assertThat(path, sameInstance(result));
    }

}
