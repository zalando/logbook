package org.zalando.logbook;

/*
 * #%L
 * Logbook: API
 * %%
 * Copyright (C) 2015 Zalando SE
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.junit.Test;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class HttpMessageTest {

    @Test
    public void shouldDelegateBodyAsStringToBody() throws IOException {
        final HttpMessage message = mock(HttpMessage.class);

        when(message.getCharset()).thenReturn(UTF_8);
        when(message.getBody()).thenReturn("foo".getBytes(UTF_8));
        when(message.getBodyAsString()).thenCallRealMethod();

        assertThat(message.getBodyAsString(), is("foo"));
    }

}
