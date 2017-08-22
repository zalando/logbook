package org.zalando.logbook.servlet;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Reflection here is used to support both Servlet API 2.5 and 3.0 in the same code base.
 */
public class AsyncHelper {

    public static boolean isFirstRequest(HttpServletRequest request) {
        return dispatcherTypeClass()
                .map(theClass -> isAsyncDispatcherType(request, theClass))
                .orElse(true);
    }

    public static boolean isLastRequest(final HttpServletRequest request) {
        return !dispatcherTypeClass().isPresent() || (dispatcherTypeClass().isPresent() && !isAsyncStarted(request));
    }

    public static void setDispatcherTypeAsync(final HttpServletRequest request) {
        dispatcherTypeClass().ifPresent(dispatcherTypeClass -> {
            Object asyncDispatcherType = enumConstant(dispatcherTypeClass, "ASYNC");
            invoke(request, "setDispatcherType", void.class, new Object[]{asyncDispatcherType}, dispatcherTypeClass);
        });
    }

    private static boolean isAsyncDispatcherType(HttpServletRequest request, Class<?> dispatcherTypeClass) {
        Object dispatcherType = invoke(request, "getDispatcherType", Object.class, new Object[]{});
        Object asyncDispatcherType = enumConstant(dispatcherTypeClass, "ASYNC");
        return !Objects.equals(dispatcherType, asyncDispatcherType);
    }

    private static boolean isAsyncStarted(HttpServletRequest request) {
        return invoke(request, "isAsyncStarted", Boolean.class, new Object[]{});
    }

    private static <T> T invoke(HttpServletRequest request, String methodName, Class<T> returnType, Object[] arguments, Class<?>... parameterTypes) {
        try {
            return returnType.cast(request.getClass().getMethod(methodName, parameterTypes).invoke(request, arguments));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Object enumConstant(Class<?> dispatcherType, String name) {
        return Arrays.stream(dispatcherType.getEnumConstants())
                .filter(constant -> constant.toString().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find DispatcherType." + name));
    }

    private static Optional<Class<?>> dispatcherTypeClass() {
        try {
            return Optional.of(Class.forName("javax.servlet.DispatcherType"));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }
}
