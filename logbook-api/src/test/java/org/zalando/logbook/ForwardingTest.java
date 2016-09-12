package org.zalando.logbook;

import lombok.SneakyThrows;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.function.UnaryOperator;

import static org.mockito.Mockito.verify;

public final class ForwardingTest {

    @Test
    public void shouldForwardRawHttpRequests() {
        test(RawHttpRequest.class, request -> (ForwardingRawHttpRequest) () -> request);
    }

    @Test
    public void shouldForwardRawHttpResponses() {
        test(RawHttpResponse.class, response -> (ForwardingRawHttpResponse) () -> response);
    }

    @Test
    public void shouldForwardHttpRequests() {
        test(HttpRequest.class, request -> (ForwardingHttpRequest) () -> request);
    }

    @Test
    public void shouldForwardHttpResponses() {
        test(HttpResponse.class, response -> (ForwardingHttpResponse) () -> response);
    }

    @SneakyThrows
    static <T> void test(final Class<T> type, final UnaryOperator<T> forwarder) {
        final T delegate = Mockito.mock(type);
        final T forwarded = forwarder.apply(delegate);

        for (final Method method : type.getMethods()) {
            method.invoke(forwarded);
            method.invoke(verify(delegate));
        }
    }

}
