package org.zalando.logbook;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public final class BodyObfuscatorTest {

    @Test
    public void noneShouldDefaultToNoOp() {
        final BodyObfuscator unit = BodyObfuscator.none();

        assertThat(unit.obfuscate("text/plain", "Hello, world!"), is(equalTo("Hello, world!")));
    }

}
