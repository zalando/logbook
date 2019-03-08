package org.zalando.logbook.lle;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;

/**
 * Simple utility which works together with the logback-test.xml configuration for
 * capturing log output in serialized (byte) form.
 */

public class StaticAppender extends OutputStreamAppender<ILoggingEvent> {
    
    private static ByteArrayOutputStream bout = new ByteArrayOutputStream();

    public static ByteArrayOutputStream getStaticOutputStream() {
        return bout;
    }
    
    public static void reset() {
        bout.reset();
    }
    
    public static String getLastStatement() {
        String content = new String(bout.toByteArray(), StandardCharsets.UTF_8); 
        return content.substring(content.lastIndexOf('\n', content.length() - 2) + 1);
    }
    
    public StaticAppender() {
        setOutputStream(bout);
    }
}