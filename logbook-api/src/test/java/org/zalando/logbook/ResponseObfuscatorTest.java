package org.zalando.logbook;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class ResponseObfuscatorTest {

    @Test
    public void noneShouldDefaultToNoOp() {
        final ResponseObfuscator unit = ResponseObfuscator.none();
        final HttpResponse response = mock(HttpResponse.class);

        assertThat(unit.obfuscate(response), is(sameInstance(response)));
    }

}