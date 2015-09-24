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
public final class ResponseData {

    private final int status;
    private final Map<String, List<String>> headers;
    private final String contentType;
    private final String body;

    ResponseData(final int status, final Map<String, List<String>> headers, final String contentType, final String body) {
        this.status = status;
        this.headers = headers;
        this.contentType = contentType;
        this.body = body;
    }

    public int getStatus() {
        return status;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public String getContentType() {
        return contentType;
    }

    public String getBody() {
        return body;
    }
}
