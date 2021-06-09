package org.zalando.logbook.httpclient5;

import lombok.experimental.UtilityClass;

import java.nio.ByteBuffer;

@UtilityClass
class ByteBufferUtils {

    static void fixedSizeCopy(ByteBuffer src, byte[] dest) {
        if (src.hasArray()) {
            byte[] array = src.array();
            System.arraycopy(array, 0, dest, 0, dest.length);
        } else {
            src.get(dest, 0, dest.length);
            src.flip();
        }
    }

}
