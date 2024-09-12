package org.zalando.logbook.openfeign;

import java.io.Closeable;
import java.io.IOException;

public class Utils {
    private Utils() {
    }

    public static void ensureClosed(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
                // ignore
            }
        }
    }
}
