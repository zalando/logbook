package org.zalando.logbook.spring;

import java.lang.reflect.Method;

public class ReflectionUtil {
    public static Method resolveMethod(Class<?> responseClass, String methodName) throws NoSuchMethodException {
        return responseClass.getMethod(methodName);
    }
}
