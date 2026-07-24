package org.zalando.logbook.httpclient5;

import lombok.experimental.UtilityClass;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

@UtilityClass
class ByteBufferUtils {

    static int fixedSizeCopy(ByteBuffer src, byte[] dest) {
        if (src.hasArray()) {
            byte[] array = src.array();
            System.arraycopy(array, 0, dest, 0, dest.length);
        } else {
            src.get(dest, 0, dest.length);
            src.flip();
        }
        return dest.length;
    }

    static int fixedSizeCopy(ByteBuffer src, ByteArrayOutputStream dest) {
        if (src.hasArray()) {
            byte[] array = src.array();
            dest.write(array, 0, array.length);
            return array.length;
        } else {
            byte[] bytes = new byte[src.remaining()];
            src.get(bytes, 0, bytes.length);
            dest.write(bytes, 0, bytes.length);
            return bytes.length;
        }
    }

}
