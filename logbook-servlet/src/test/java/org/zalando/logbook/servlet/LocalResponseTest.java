package org.zalando.logbook.servlet;

/*
 * #%L
 * Logbook
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

import java.io.IOException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LocalResponseTest {

    @Test
    public void shouldUseSameBody() throws IOException {
        final HttpServletResponse mock = mock(HttpServletResponse.class);
        when(mock.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
            }
        });

        final LocalResponse response = new LocalResponse(mock, "1");
        response.getOutputStream().write("test".getBytes());

        final byte[] body1 = response.getBody();
        final byte[] body2 = response.getBody();

        assertSame(body1, body2);
    }
}
