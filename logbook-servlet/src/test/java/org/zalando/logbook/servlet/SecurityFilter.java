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

import javax.annotation.Nullable;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SecurityFilter implements HttpFilter {

    @Nullable
    private Integer status;

    @Override
    public void doFilter(HttpServletRequest httpRequest, HttpServletResponse httpResponse, FilterChain chain)
            throws ServletException, IOException {

        if (status == null) {
            chain.doFilter(httpRequest, httpResponse);
        } else {
            httpResponse.setStatus(status);
        }
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
