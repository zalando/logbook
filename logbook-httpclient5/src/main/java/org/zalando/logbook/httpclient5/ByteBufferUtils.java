package org.zalando.logbook.httpclient5;

import lombok.experimental.UtilityClass;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

@UtilityClass
class ByteBufferUtils {

    static int fixedSizeCopy(ByteBuffer src, byte[] dest) {
        if (src.hasArray()) {
            byte[] array = src.array();
            int offset = src.arrayOffset() + src.position();
            int length = src.remaining();
            System.arraycopy(array, offset, dest, 0, length);
            return length;
        } else {
            src.get(dest, 0, dest.length);
            src.flip();
            return dest.length;
        }
    }

    static int fixedSizeCopy(ByteBuffer src, ByteArrayOutputStream dest) {
        if (src.hasArray()) {
            byte[] array = src.array();
            int offset = src.arrayOffset() + src.position();
            int length = src.remaining();
            dest.write(array, offset, length);
            return length;
        } else {
            byte[] bytes = new byte[src.remaining()];
            src.get(bytes, 0, bytes.length);
            dest.write(bytes, 0, bytes.length);
            return bytes.length;
        }
    }

}
