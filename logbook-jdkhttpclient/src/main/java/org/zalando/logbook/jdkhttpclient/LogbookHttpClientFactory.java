package org.zalando.logbook.jdkhttpclient;

import org.zalando.logbook.Logbook;

import java.net.http.HttpClient;

public final class LogbookHttpClientFactory {

    private final Logbook logbook;
    private final boolean decompressResponse;

    public LogbookHttpClientFactory(final Logbook logbook, final boolean decompressResponse) {
        this.logbook = logbook;
        this.decompressResponse = decompressResponse;
    }

    public HttpClient wrap(final HttpClient delegate) {
        return LogbookHttpClient.wrap(delegate, logbook, decompressResponse);
    }

    public HttpClient create() {
        return wrap(HttpClient.newHttpClient());
    }

}
