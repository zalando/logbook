package org.zalando.logbook.spring;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Note: Mockito can't mock java.lang.* things well, this class is easier to mock.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtil {
    public static Method[] resolveMethodChain(Class<?> responseClass, String methodName1, String methodName2) throws NoSuchMethodException {
        Method method1 = responseClass.getMethod(methodName1);
        Method method2 = method1.getReturnType().getMethod(methodName2);
        return new Method[]{method1, method2};
    }

    public static Object invokeChain(Object object, Method[] methodArray) throws InvocationTargetException, IllegalAccessException {
        Object result = object;
        for (Method method : methodArray) {
            result = method.invoke(result);
        }
        return result;
    }
}
