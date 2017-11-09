package org.zalando.logbook.servlet;

import java.util.function.Supplier;

final class ClassPath {

    static <T> T load(final String name, final Supplier<? extends T> present, final Supplier<? extends T> absent) {
        return exists(name) ? present.get() : absent.get();
    }

    private static boolean exists(final String name) {
        try {
            Class.forName(name);
            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }

}
