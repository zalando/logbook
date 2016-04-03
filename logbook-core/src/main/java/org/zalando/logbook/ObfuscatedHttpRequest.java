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

import com.google.gag.annotation.remark.Hack;
import com.google.gag.annotation.remark.OhNoYouDidnt;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

final class ObfuscatedHttpRequest extends ForwardingHttpRequest {

    private final HttpRequest request;
    private final Obfuscator headerObfuscator;
    private final Obfuscator parameterObfuscator;
    private final BodyObfuscator bodyObfuscator;

    ObfuscatedHttpRequest(final HttpRequest request, final Obfuscator headerObfuscator,
            final Obfuscator parameterObfuscator, final BodyObfuscator bodyObfuscator) {
        this.request = request;
        this.headerObfuscator = headerObfuscator;
        this.parameterObfuscator = parameterObfuscator;
        this.bodyObfuscator = bodyObfuscator;
    }

    @Override
    protected HttpRequest delegate() {
        return request;
    }

    @Override
    public String getRequestUri() {
        final String requestUri = super.getRequestUri();

        final URI parsedUri;
        try {
            parsedUri = new URI(requestUri);
        } catch (final URISyntaxException invalid) {
            // It's an invalid URI, so the parameters
            // cannot be extracted for obfuscation.
            return requestUri;
        }

        final QueryParameters parameters = QueryParameters.parse(parsedUri.getQuery());

        if (parameters.isEmpty()) {
            return requestUri;
        }

        final String queryString = parameters.obfuscate(parameterObfuscator).toString();

        return createUri(parsedUri, queryString).toASCIIString();
    }

//    @VisibleForTesting
    @SuppressWarnings("ConstantConditions")
    static URI createUri(@Nullable final URI uri, final String queryString) {
        try {
            return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), queryString, uri.getFragment());
        } catch (@Hack("Just so we can trick the code coverage") @OhNoYouDidnt final Exception e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public Multimap<String, String> getHeaders() {
        return obfuscate(delegate().getHeaders(), headerObfuscator);
    }

    private Multimap<String, String> obfuscate(final Multimap<String, String> values, final Obfuscator obfuscator) {
        return Util.transformEntries(values, obfuscator::obfuscate);
    }

    @Override
    public byte[] getBody() throws IOException {
        return getBodyAsString().getBytes(getCharset());
    }

    @Override
    public String getBodyAsString() throws IOException {
        return bodyObfuscator.obfuscate(getContentType(), request.getBodyAsString());
    }

}
