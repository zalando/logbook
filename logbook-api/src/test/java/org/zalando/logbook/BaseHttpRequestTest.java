package org.zalando.logbook;

/*
 * #%L
 * Logbook: API
 * %%
 * Copyright (C) 2015 - 2016 Zalando SE
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

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public final class BaseHttpRequestTest {

    @Test
    public void shouldReconstructURI() {
        final BaseHttpRequest unit = spy(BaseHttpRequest.class);
        when(unit.getScheme()).thenReturn("http");
        when(unit.getHost()).thenReturn("localhost");
        when(unit.getPort()).thenReturn(Optional.empty());
        when(unit.getPath()).thenReturn("/test");
        when(unit.getQuery()).thenReturn("limit=1");

        assertThat(unit.getRequestUri(), is("http://localhost/test?limit=1"));
    }

}