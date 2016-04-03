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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Value
@Getter
@Builder
@NoArgsConstructor(staticName = "create")
public class MockRawHttpRequest implements RawHttpRequest {

    private Multimap<String, String> headers = Util.of();
    private String contentType = "";
    private Charset charset = StandardCharsets.UTF_8;
    private Origin origin = Origin.REMOTE;
    private String remote = "127.0.0.1";
    private String method = "GET";
    private String requestUri = "http://localhost/";

    @Override
    public HttpRequest withBody() throws IOException {
        return MockHttpRequest.builder()
                .headers(headers)
                .contentType(contentType)
                .charset(charset)
                .origin(origin)
                .remote(remote)
                .method(method)
                .requestUri(requestUri)
                .build();
    }

}
