package org.zalando.logbook.servlet;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;

import javax.servlet.DispatcherType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;

final class RequestBuilders {

    static RequestBuilder async(final MvcResult result) {
        final RequestBuilder builder = asyncDispatch(result);

        return context -> {
            final MockHttpServletRequest request = builder.buildRequest(context);
            // this is missing in MockMvcRequestBuilders#asyncDispatch
            request.setDispatcherType(DispatcherType.ASYNC);
            return request;
        };
    }

}
