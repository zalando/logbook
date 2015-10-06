package org.zalando.logbook.spring;

/*
 * #%L
 * Logbook: Spring
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public final class ForwardingClientHttpResponseTest {

    private final ClientHttpResponse unit = new ForwardingClientHttpResponse() {
        @Override
        protected ClientHttpResponse delegate() {
            return new ClientHttpResponse() {
                @Override
                public HttpStatus getStatusCode() {
                    return HttpStatus.OK;
                }

                @Override
                public int getRawStatusCode() {
                    return 200;
                }

                @Override
                public String getStatusText() {
                    return "OK";
                }

                @Override
                public void close() {

                }

                @Override
                public InputStream getBody() {
                    return null;
                }

                @Override
                public HttpHeaders getHeaders() {
                    return new HttpHeaders();
                }
            };
        }
    };

    @Test
    public void shouldDelegate() throws IOException {
        assertThat(unit.getStatusCode(), is(HttpStatus.OK));
        assertThat(unit.getRawStatusCode(), is(200));
        assertThat(unit.getStatusText(), is("OK"));
        assertThat(unit.getBody(), is(nullValue()));
        assertThat(unit.getHeaders().toSingleValueMap().values(), is(empty()));
    }

}