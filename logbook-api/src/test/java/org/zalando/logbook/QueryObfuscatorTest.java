package org.zalando.logbook;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public final class QueryObfuscatorTest {

    @Test
    public void noneShouldDefaultToNoOp() {
        final QueryObfuscator unit = QueryObfuscator.none();

        assertThat(unit.obfuscate("a=b&c=d&f=e"), is(equalTo("a=b&c=d&f=e")));
    }

}