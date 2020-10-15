package org.zalando.logbook.logstash;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Simple utility which works together with the logback-test.xml configuration for
 * capturing pretty-printed log output in serialized (byte) form.
 */
public final class PrettyPrintingStaticAppender extends ConsoleAppender<ILoggingEvent> {
    
    private static final ByteArrayOutputStream stream = new ByteArrayOutputStream();

    static void reset() {
        stream.reset();
    }
    
    static String getLastStatement() {
        final String content = new String(stream.toByteArray(), StandardCharsets.UTF_8);
        return content.substring(content.lastIndexOf("\n{\n", content.length()) + 1);
    }
    
    @Override
	public void setOutputStream(final OutputStream ignored) {
		super.setOutputStream(stream);
	}
}
