package org.zalando.logbook;

/*
 * #%L
 * Logbook: Core
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

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.IOException;
import java.net.URI;

@Value
@Getter
@Builder
@NoArgsConstructor(staticName = "create")
public class MockRawHttpRequest implements RawHttpRequest {

    private String remote = "127.0.0.1";
    private String method = "GET";
    private URI requestUri = URI.create("http://localhost/");

    @Override
    public HttpRequest withBody() throws IOException {
        return MockHttpRequest.builder()
                .remote(remote)
                .method(method)
                .requestUri(requestUri.toASCIIString())
                .build();
    }

}
