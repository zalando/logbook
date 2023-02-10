package org.zalando.logbook.openfeign;

import feign.RequestLine;

public interface FeignClient {
    @RequestLine("GET /get/string")
    String getString();

    @RequestLine("GET /get/void")
    void getVoid();

    @RequestLine("POST /post/bad-request")
    String postBadRequest(String request);
}
