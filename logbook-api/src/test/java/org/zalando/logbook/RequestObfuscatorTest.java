package org.zalando.logbook;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class RequestObfuscatorTest {

    @Test
    public void noneShouldDefaultToNoOp() {
        final RequestObfuscator unit = RequestObfuscator.none();
        final HttpRequest request = mock(HttpRequest.class);

        assertThat(unit.obfuscate(request), is(sameInstance(request)));
    }

}