package org.zalando.logbook.spring;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.zalando.logbook.Logbook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.zalando.logbook.spring.LogbookClientHttpRequestInterceptorBackwardCompatibility.of;
import static org.zalando.logbook.spring.ReflectionUtil.invokeChain;
import static org.zalando.logbook.spring.ReflectionUtil.resolveMethodChain;

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
    void getHttpResponse_resolveMethodThrows() {
        try (MockedStatic<ReflectionUtil> utilities = Mockito.mockStatic(ReflectionUtil.class)) {
            utilities.when(() -> resolveMethodChain(any(), any(), any())).thenReturn(null);

            assertThrows(RuntimeException.class, () -> of(Logbook.create())
                    .getHttpResponse(new MockClientHttpResponse(new byte[0], HttpStatus.OK)).getStatus());
        }
    }

    @Test
    void getHttpResponse_invokeThrows() {
        try (MockedStatic<ReflectionUtil> utilities = Mockito.mockStatic(ReflectionUtil.class)) {
            utilities.when(() -> invokeChain(any(), any())).thenThrow(new IllegalAccessException());

            assertThrows(RuntimeException.class, () -> of(Logbook.create())
                    .getHttpResponse(new MockClientHttpResponse(new byte[0], HttpStatus.OK)).getStatus());
        }
    }

    @Test
    void constructor_throws() {
        try (MockedStatic<ReflectionUtil> utilities = Mockito.mockStatic(ReflectionUtil.class)) {
            utilities.when(() -> resolveMethodChain(any(), any(), any())).thenThrow(new NoSuchMethodException());

            assertThrows(RuntimeException.class, () -> of(Logbook.create(), ClientHttpResponse.class));
        }
    }
}