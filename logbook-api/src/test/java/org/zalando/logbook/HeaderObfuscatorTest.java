package org.zalando.logbook;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public final class HeaderObfuscatorTest {

    @Test
    public void noneShouldDefaultToNoOp() {
        final HeaderObfuscator unit = HeaderObfuscator.none();

        assertThat(unit.obfuscate("Authorization", "Bearer c61a8f84-6834-11e5-a607-10ddb1ee7671"),
                is(equalTo("Bearer c61a8f84-6834-11e5-a607-10ddb1ee7671")));
    }

}
