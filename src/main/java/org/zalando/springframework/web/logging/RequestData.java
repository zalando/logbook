package org.zalando.springframework.web.logging;

/*
 * #%L
 * spring-web-logging
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

import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.Map;

@Immutable
public class RequestData {

    private final String remote;
    private final String method;
    private final String url;
    private final Map<String, List<String>> headers;
    private final Map<String, List<String>> parameters;
    private final String body;

    public RequestData(final String remote, final String method, final String url, final Map<String, List<String>> headers,
            final Map<String, List<String>> parameters, final String body) {
        this.remote = remote;
        this.method = method;
        this.url = url;
        this.headers = headers;
        this.parameters = parameters;
        this.body = body;
    }

    public String getRemote() {
        return remote;
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public Map<String, List<String>> getParameters() {
        return parameters;
    }

    public String getBody() {
        return body;
    }
}
