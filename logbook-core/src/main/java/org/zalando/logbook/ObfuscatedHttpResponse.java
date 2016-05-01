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

import com.google.common.collect.ListMultimap;

import java.io.IOException;

import static com.google.common.collect.Multimaps.transformEntries;

public final class ObfuscatedHttpResponse extends ForwardingHttpResponse {

    private final HttpResponse response;
    private final BodyObfuscator bodyObfuscator;
    private final ListMultimap<String, String> headers;

    ObfuscatedHttpResponse(final HttpResponse response, final HeaderObfuscator headerObfuscator,
            final BodyObfuscator bodyObfuscator) {
        this.response = response;
        this.bodyObfuscator = bodyObfuscator;
        this.headers = transformEntries(response.getHeaders(), headerObfuscator::obfuscate);
    }

    @Override
    protected HttpResponse delegate() {
        return response;
    }

    @Override
    public ListMultimap<String, String> getHeaders() {
        return headers;
    }

    @Override
    public byte[] getBody() throws IOException {
        return getBodyAsString().getBytes(getCharset());
    }

    @Override
    public String getBodyAsString() throws IOException {
        return bodyObfuscator.obfuscate(response.getContentType(), response.getBodyAsString());
    }

}
