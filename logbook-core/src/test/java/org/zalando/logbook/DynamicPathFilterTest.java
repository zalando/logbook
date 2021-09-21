package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DynamicPathFilterTest {

    @Test
    public void testDynamicFilterOneReplacement() {
        final String path = "/profiles/Zalando/user.json";
        final String expr = "/profiles/{organization}/user.json";

        final PathFilter regexpPathUriFilter = new DynamicPathFilter(String::toUpperCase, expr);

        final String result = regexpPathUriFilter.filter(path);
        assertThat(result).isEqualTo("/profiles/ZALANDO/user.json");
    }

    @Test
    public void testDynamicFilterTwoSideBySideReplacements() {
        final String path = "/profiles/Zalando/test@test.com/user.json";
        final String expr = "/profiles/{organization}/{email}/user.json";

        final PathFilter regexpPathUriFilter = new DynamicPathFilter(String::toUpperCase, expr);

        final String result = regexpPathUriFilter.filter(path);
        assertThat(result).isEqualTo("/profiles/ZALANDO/TEST@TEST.COM/user.json");
    }

    @Test
    public void testDynamicFilterTwoReplacements() {
        final String path = "/profiles/Zalando/my/test@test.com/user.json";
        final String expr = "/profiles/{organization}/my/{email}/user.json";

        final PathFilter regexpPathUriFilter = new DynamicPathFilter(String::toUpperCase, expr);

        final String result = regexpPathUriFilter.filter(path);
        assertThat(result).isEqualTo("/profiles/ZALANDO/my/TEST@TEST.COM/user.json");
    }

    @Test
    public void testDynamicFilterSubpath() {
        final String path = "/profiles/Zalando/email/test@test.com/user/extra";
        final String expr = "/profiles/{organization}/email/{email}/user";

        final PathFilter regexpPathUriFilter = new DynamicPathFilter(String::toUpperCase, expr);

        final String result = regexpPathUriFilter.filter(path);
        assertThat(result).isEqualTo("/profiles/ZALANDO/email/TEST@TEST.COM/user/extra");
    }

    @Test
    public void testDynamicFilterStart() {
        final String path = "/a/b/c/d/e";
        final String expr = "/{a}/b/c/d/e";

        final PathFilter regexpPathUriFilter = new DynamicPathFilter(String::toUpperCase, expr);

        final String result = regexpPathUriFilter.filter(path);
        assertThat(result).isEqualTo("/A/b/c/d/e");
    }

    @Test
    public void testDynamicFilterStartNoSlash() {
        final String path = "a/b/c/d/e";
        final String expr = "{a}/b/c/d/e";

        final PathFilter regexpPathUriFilter = new DynamicPathFilter(String::toUpperCase, expr);

        final String result = regexpPathUriFilter.filter(path);
        assertThat(result).isEqualTo("A/b/c/d/e");
    }

    @Test
    public void testDynamicFilterEnd() {
        final String path = "/a/b/c/d/e/";
        final String expr = "/a/b/c/d/{e}/";

        final PathFilter regexpPathUriFilter = new DynamicPathFilter(String::toUpperCase, expr);

        final String result = regexpPathUriFilter.filter(path);
        assertThat(result).isEqualTo("/a/b/c/d/E");
    }

    @Test
    public void testDynamicFilterEndPathNoSlash() {
        final String path = "/a/b/c/d/e";
        final String expr = "/a/b/c/d/{e}/";

        final PathFilter regexpPathUriFilter = new DynamicPathFilter(String::toUpperCase, expr);

        final String result = regexpPathUriFilter.filter(path);
        assertThat(result).isEqualTo("/a/b/c/d/E");
    }

    @Test
    public void testDynamicFilterEndExpressionNoSlash() {
        final String path = "/a/b/c/d/e/";
        final String expr = "/a/b/c/d/{e}";

        final PathFilter regexpPathUriFilter = new DynamicPathFilter(String::toUpperCase, expr);

        final String result = regexpPathUriFilter.filter(path);
        assertThat(result).isEqualTo("/a/b/c/d/E");
    }

    @Test
    public void testDynamicFilterEndNoSlash() {
        final String path = "/a/b/c/d/e";
        final String expr = "/a/b/c/d/{e}";

        final PathFilter regexpPathUriFilter = new DynamicPathFilter(String::toUpperCase, expr);

        final String result = regexpPathUriFilter.filter(path);
        assertThat(result).isEqualTo("/a/b/c/d/E");
    }

    @Test
    public void testDynamicReturnsInputInstance() {
        final String path = "/b/c/d/e/f";
        final String expr = "/a/b/{c}/d/e";

        final PathFilter regexpPathUriFilter = new DynamicPathFilter(String::toUpperCase, expr);

        final String result = regexpPathUriFilter.filter(path);
        assertThat(result).isSameAs(path);
    }

    @Test
    public void testDynamicIgnoresIncorrectExpression1() {
        final String path = "/a/b/c/d/e";
        final String expr = "/a/b/{c/d/e";

        final PathFilter regexpPathUriFilter = new DynamicPathFilter(String::toUpperCase, expr);

        final String result = regexpPathUriFilter.filter(path);
        assertThat(path).isSameAs(result);
    }

    @Test
    public void testDynamicIgnoresIncorrectExpression2() {
        final String path = "/a/b/c/d/e";
        final String expr = "/a/b/c}/d/e";

        final PathFilter regexpPathUriFilter = new DynamicPathFilter(String::toUpperCase, expr);

        final String result = regexpPathUriFilter.filter(path);
        assertThat(path).isSameAs(result);
    }

    @Test
    public void testDynamicPathEndsAtSubstitute() {
        final String path = "/profile/Zalando";
        final String expr = "/profile/{id}/info";

        final PathFilter regexpPathUriFilter = new DynamicPathFilter(String::toUpperCase, expr);

        final String result = regexpPathUriFilter.filter(path);
        assertThat(result).isEqualTo(path);
    }

}