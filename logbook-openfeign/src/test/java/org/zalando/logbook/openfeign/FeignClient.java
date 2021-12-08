package org.zalando.logbook.openfeign;

import feign.RequestLine;

public interface FeignClient {
    @RequestLine("GET /get/string")
    String getString();

    @RequestLine("GET /get/string")
    void getVoid();

    @RequestLine("GET /get/empty")
    void getEmptyBody();

    @RequestLine("POST /post/bad-request")
    String postBadRequest(String request);
}
