package org.zalando.logbook.spring;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.zalando.logbook.Logbook;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.zalando.logbook.spring.LogbookClientHttpRequestInterceptorBackwardCompatibility.of;
import static org.zalando.logbook.spring.ReflectionUtil.resolveMethod;

class LogbookClientHttpRequestInterceptorBackwardCompatibilityTest {

    @Test
    void getHttpResponseFromClientHttpResponse() {
        assertThat(of(Logbook.create())
                .getHttpResponse(new MockClientHttpResponse(new byte[0], HttpStatus.OK)).getStatus())
                .isEqualTo(200);
    }

    @Test
    void getHttpResponseFromMockClientHttpResponse() {
        assertThat(of(Logbook.create(), MockClientHttpResponse.class)
                .getHttpResponse(new MockClientHttpResponse(new byte[0], HttpStatus.UNAUTHORIZED)).getStatus())
                .isEqualTo(401);
    }

    @Test
    void getHttpResponse_throws() throws InvocationTargetException, IllegalAccessException {
        try (MockedStatic<ReflectionUtil> utilities = Mockito.mockStatic(ReflectionUtil.class)) {
            Method method = mock(Method.class);
            doThrow(IllegalAccessException.class).when(method).invoke(any(Object.class));
            utilities.when(() -> resolveMethod(any(Class.class), anyString())).thenReturn(method);

            assertThrows(RuntimeException.class, () -> of(Logbook.create())
                    .getHttpResponse(new MockClientHttpResponse(new byte[0], HttpStatus.OK)).getStatus());
        }
    }

    @Test
    void constructor_throws() {
        try (MockedStatic<ReflectionUtil> utilities = Mockito.mockStatic(ReflectionUtil.class)) {
            utilities.when(() -> resolveMethod(any(Class.class), anyString())).thenThrow(new NoSuchMethodException());

            assertThrows(RuntimeException.class, () -> of(Logbook.create(), ClientHttpResponse.class));
        }
    }
}