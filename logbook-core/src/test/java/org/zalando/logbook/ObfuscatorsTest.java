package org.zalando.logbook;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ObfuscatorsTest {

    @Test
    public void accessTokenShouldObfuscateAccessTokenParameter() {
        final QueryObfuscator unit = Obfuscators.accessToken();

        assertThat(unit.obfuscate("name=alice&access_token=bob"), is(equalTo("name=alice&access_token=XXX")));
    }

    @Test
    public void shouldOnlyObfuscateHeaderIfBothNameAndValueApply() {
        final HeaderObfuscator unit = Obfuscators.obfuscate((name, value) ->
                "name".equals(name) && "Alice".equals(value), "<secret>");

        assertThat(unit.obfuscate("name", "Alice"), is("<secret>"));
        assertThat(unit.obfuscate("name", "Bob"), is("Bob"));
    }

    @Test
    public void authorizationShouldObfuscateAuthorization() {
        final HeaderObfuscator unit = Obfuscators.authorization();

        assertThat(unit.obfuscate("Authorization", "Bearer c61a8f84-6834-11e5-a607-10ddb1ee7671"),
                is(equalTo("XXX")));
    }

    @Test
    public void authorizationShouldNotObfuscateNonAuthorization() {
        final HeaderObfuscator unit = Obfuscators.authorization();

        assertThat(unit.obfuscate("Accept", "text/plain"), is(equalTo("text/plain")));
    }

    @Test
    public void shouldObfuscateHeaders() {
        final Map<String, List<String>> m = Obfuscators.obfuscateHeaders(
            MockHeaders.of("hello", "world", "chao", "bambina"),
            (k, v) -> v.toUpperCase()
        );

        assertEquals(2, m.size());
        assertTrue(m.containsKey("hello"));
        assertTrue(m.containsKey("chao"));
        assertEquals(singletonList("WORLD"), m.get("hello"));
        assertEquals(singletonList("BAMBINA"), m.get("chao"));
    }
}
