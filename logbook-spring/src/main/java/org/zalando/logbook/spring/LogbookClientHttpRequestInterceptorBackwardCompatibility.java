package org.zalando.logbook.spring;

import org.springframework.http.client.ClientHttpResponse;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Logbook;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.zalando.logbook.spring.ReflectionUtil.resolveMethod;

/**
 * Supports sprint framework older than 6.2 (should support 4.X)
 */
public class LogbookClientHttpRequestInterceptorBackwardCompatibility extends LogbookClientHttpRequestInterceptor {
    private final Method getStatusCodeMethod;
    private final Method valueMethod;

    public LogbookClientHttpRequestInterceptorBackwardCompatibility(Logbook logbook) {
        this(logbook, ClientHttpResponse.class);
    }

    protected LogbookClientHttpRequestInterceptorBackwardCompatibility(Logbook logbook, Class<?> responseClass) {
        super(logbook);
        try {
            getStatusCodeMethod = resolveMethod(responseClass, "getStatusCode");
            valueMethod = resolveMethod(getStatusCodeMethod.getReturnType(), "value");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected HttpResponse getHttpResponse(ClientHttpResponse clientHttpResponse) {
        final int statusCode = getStatus(clientHttpResponse);
        return new RemoteResponse(clientHttpResponse) {
            @Override
            public int getStatus() {
                return statusCode;
            }
        };
    }

    private int getStatus(ClientHttpResponse clientHttpResponse) {
        try {
            Object statusCode = getStatusCodeMethod.invoke(clientHttpResponse);
            return (int) valueMethod.invoke(statusCode);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static LogbookClientHttpRequestInterceptorBackwardCompatibility of(Logbook logbook, Class<?> responseClass) {
        return new LogbookClientHttpRequestInterceptorBackwardCompatibility(logbook, responseClass);
    }

    public static LogbookClientHttpRequestInterceptorBackwardCompatibility of(Logbook logbook) {
        return new LogbookClientHttpRequestInterceptorBackwardCompatibility(logbook);
    }
}
