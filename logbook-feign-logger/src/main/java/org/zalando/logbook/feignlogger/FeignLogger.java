/*
 * Copyright 2013 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.logbook.feignlogger;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.logbook.Correlator;
import org.zalando.logbook.Logbook;

import feign.Request;
import feign.Response;

/**
 * Logs to SLF4J. 
 */
public class FeignLogger extends feign.Logger {

    /*
     * 
     * Implementation note: There is no way to set an attribute on a feign request, so using a thread local instead.
     * 
     */
    
    protected ThreadLocal<Optional<Correlator>> threadLocal = new InheritableThreadLocal<Optional<Correlator>>() {
        
        @Override
        protected Optional<Correlator> childValue(Optional<Correlator> parentValue) {
            return parentValue;
        }
    };
    
    private final Logger logger;
    private final Logbook logbook;

    
    public FeignLogger(Logbook logbook) {
        this(feign.Logger.class, logbook);
    }

    public FeignLogger(Class<?> clazz, Logbook logbook) {
        this(LoggerFactory.getLogger(clazz), logbook);
    }

    public FeignLogger(String name, Logbook logbook) {
        this(LoggerFactory.getLogger(name), logbook);
    }

    public FeignLogger(Logger logger, Logbook logbook) {
        this.logger = logger;
        this.logbook = logbook;
    }
    
    @Override
    protected void logRequest(String configKey, Level logLevel, Request feignRequest) {
        LocalRequest rawHttpRequest = new LocalRequest(feignRequest, Localhost.resolve());

        try {
            threadLocal.set(logbook.write(rawHttpRequest));
        } catch (IOException e) {
            logger.error("Problem writing HTTP request", e);
        }
    }

    @Override
    protected Response logAndRebufferResponse(String configKey, Level logLevel, Response feignResponse, long elapsedTime) throws IOException {
        Optional<Correlator> optional = threadLocal.get();
        
        if(optional != null) {
            optional.ifPresent(r -> new RemoteResponse(feignResponse));
        }

        return feignResponse;
    }

    @Override
    protected void log(String configKey, String format, Object... args) {
        throw new RuntimeException(); // should never happen
    }

}
