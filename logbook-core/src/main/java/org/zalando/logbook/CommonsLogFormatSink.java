package org.zalando.logbook;

import lombok.AllArgsConstructor;
import org.apiguardian.api.API;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoField.YEAR;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * @see <a href="https://en.wikipedia.org/wiki/Common_Log_Format">Wikipedia: Common Log Format</a>
 * @see <a href="https://httpd.apache.org/docs/trunk/logs.html#common">Apache HTTP Server: Common Log Format</a>
 */
@API(status = EXPERIMENTAL)
@AllArgsConstructor
public final class CommonsLogFormatSink implements Sink {

    private final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendLiteral('[')
            .appendValue(DAY_OF_MONTH, 2)
            .appendLiteral('/')
            .appendText(MONTH_OF_YEAR, TextStyle.SHORT)
            .appendLiteral('/')
            .appendValue(YEAR, 4)
            .appendLiteral(':')
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
            .appendLiteral(' ')
            .appendOffset("+HHMM", "0000")
            .appendLiteral(']')
            .toFormatter();

    private final HttpLogWriter writer;
    private final ZoneId timeZone;

    public CommonsLogFormatSink(final HttpLogWriter writer) {
        this(writer, ZoneId.systemDefault());
    }

    @Override
    public boolean isActive() {
        return writer.isActive();
    }

    @Override
    public void write(final Precorrelation precorrelation, final HttpRequest request) {
        // nothing to do...
    }

    @Override
    public void write(final Correlation correlation, final HttpRequest request,
            final HttpResponse response) throws IOException {

        final StringBuilder output = new StringBuilder(120);

        // client's IP address (or proxy)
        output.append(request.getRemote());
        output.append(' ');

        // remote user identity (RFC 1413)
        output.append('-');
        output.append(' ');

        // user id from HTTP authentication
        // TODO https://github.com/zalando/logbook/issues/381
        output.append('-');
        output.append(' ');

        // The time that the request was received.
        formatter.formatTo(correlation.getStart().atZone(timeZone), output);
        output.append(' ');

        // request line
        output.append('"');
        {
            output.append(request.getMethod());
            output.append(' ');
            output.append(request.getPath());

            final String query = request.getQuery();

            if (!query.isEmpty()) {
                output.append('?');
                output.append(query);
            }

            output.append(' ');
            output.append(request.getProtocolVersion());
        }
        output.append('"');

        output.append(' ');
        output.append(response.getStatus());
        output.append(' ');

        final int bytes = response.getBody().length;

        if (bytes == 0) {
            output.append('-');
        } else {
            output.append(bytes);
        }

        writer.write(correlation, output.toString());
    }

}
