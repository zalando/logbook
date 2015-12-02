package org.zalando.logbook.servlet;

/*
 * #%L
 * Logbook: Servlet
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

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;

import javax.servlet.DispatcherType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;

final class RequestBuilders {

    static RequestBuilder async(final MvcResult result) throws Exception {
        final RequestBuilder builder = asyncDispatch(result);

        return context -> {
            final MockHttpServletRequest request = builder.buildRequest(context);
            // this is missing in MockMvcRequestBuilders#asyncDispatch
            request.setDispatcherType(DispatcherType.ASYNC);
            return request;
        };
    }

}
