package org.zalando.logbook.logstash;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.output.TeeOutputStream;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;

/**
 * Simple utility which works together with the logback-test.xml configuration for
 * capturing pretty-printed log output in serialized (byte) form.
 */

public class PrettyPrintingStaticAppender extends ConsoleAppender<ILoggingEvent> {
    
    private static ByteArrayOutputStream bout = new ByteArrayOutputStream();

    public static ByteArrayOutputStream getStaticOutputStream() {
        return bout;
    }
    
    public static void reset() {
        bout.reset();
    }
    
    public static String getLastStatement() {
        String content = new String(bout.toByteArray(), StandardCharsets.UTF_8); 
        
        return content.substring(content.lastIndexOf("\n{\n", content.length()) + 1);
    }
    
    @Override
	public void setOutputStream(OutputStream outputStream) {
		super.setOutputStream(new TeeOutputStream(outputStream, bout));
	}
}