package org.zalando.logbook.jaxrs;

import lombok.Value;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;

@Value
class RoundTrip {
    HttpRequest clientRequest;
    HttpResponse clientResponse;
    HttpRequest serverRequest;
    HttpResponse serverResponse;
}
