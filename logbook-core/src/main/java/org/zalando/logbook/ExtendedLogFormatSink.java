package org.zalando.logbook;

import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * @see <a href="https://en.wikipedia.org/wiki/Extended_Log_Format">Wikipedia: Extended Log Format</a>
 * @see <a href="https://www.w3.org/TR/WD-logfile.html">W3C Extended Log Format</a>
 */
@API(status = EXPERIMENTAL)
public final class ExtendedLogFormatSink implements Sink {

    private static final String DELIMITER = " ";
    private static final String HEADER_DELIMITER = ";";
    private static final String OMITTED_FIELD = "-";
    private static final String DEFAULT_VERSION = "1.0";
    private static final String DEFAULT_FIELDS = "date time c-ip s-dns cs-method cs-uri-stem cs-uri-query sc-status sc-bytes cs-bytes time-taken cs-protocol cs(User-Agent) cs(Cookie) cs(Referrer)";
    private static final Pattern CS_HEADER_REGEX = Pattern.compile("cs\\((.*?)\\)");
    private static final Pattern SC_HEADER_REGEX = Pattern.compile("sc\\((.*?)\\)");

    private final HttpLogWriter writer;
    private final ZoneId timeZone;
    private final List<String> supportedFields;
    private final List<String> fields;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME;

    public ExtendedLogFormatSink(final HttpLogWriter writer) {
        this(writer, ZoneId.of("UTC"), DEFAULT_VERSION, DEFAULT_FIELDS);
    }

    public ExtendedLogFormatSink(final HttpLogWriter writer, final String fields) {
        this(writer, ZoneId.of("UTC"), DEFAULT_VERSION, fields);
    }

    public ExtendedLogFormatSink(final HttpLogWriter writer, ZoneId timeZone, final String version,
                                 final String fields) {
        this.writer = writer;
        this.timeZone = timeZone;
        this.supportedFields = getSupportedFields();
        this.fields = getFields(fields);
        logDirectives(version, this.fields);
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

        final ZonedDateTime startTime = correlation.getStart().atZone(timeZone);

        // date
        final String date = dateFormatter.format(startTime);

        // time
        final String time = timeFormatter.format(startTime);

        // time-taken
        final String timeTaken = BigDecimal
                .valueOf(correlation.getDuration().toMillis())
                .divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP)
                .toString();

        // cs-protocol
        final String csProtocol = request.getProtocolVersion();

        // sc-bytes
        final String scBytes = String.valueOf(response.getBody().length);

        // cs-bytes
        final String csBytes = String.valueOf(request.getBody().length);

        // c-ip
        final String clientIp = request.getRemote();

        // s-dns
        final String serverDns = request.getHost();

        // sc-status
        final String status = String.valueOf(response.getStatus());

        // sc-comment
        final String comment = response.getReasonPhrase();

        // cs-method
        final String method = request.getMethod();

        // cs-uri
        final String uri = request.getRequestUri();

        // cs-uri-stem
        final String uriStem = request.getPath();

        // cs-uri-query
        final String uriQuery;
        if (!"".equals(request.getQuery())) {
            uriQuery = "?" + request.getQuery();
        } else {
            uriQuery = OMITTED_FIELD;
        }

        Map<String, String> fieldValMap = new HashMap<>();
        fieldValMap.put(Field.DATE.value, date);
        fieldValMap.put(Field.TIME.value, time);
        fieldValMap.put(Field.TIME_TAKEN.value, timeTaken);
        fieldValMap.put(Field.CS_PROTOCOL.value, csProtocol);
        fieldValMap.put(Field.SC_BYTES.value, scBytes);
        fieldValMap.put(Field.CS_BYTES.value, csBytes);
        fieldValMap.put(Field.CLIENT_IP.value, clientIp);
        fieldValMap.put(Field.SERVER_DNS.value, serverDns);
        fieldValMap.put(Field.RESP_STATUS.value, status);
        fieldValMap.put(Field.RESP_COMMENT.value, comment);
        fieldValMap.put(Field.REQ_METHOD.value, method);
        fieldValMap.put(Field.REQ_URI.value, uri);
        fieldValMap.put(Field.REQ_URI_STEM.value, uriStem);
        fieldValMap.put(Field.REQ_URI_QUERY.value, uriQuery);

        final List<String> outputFields = new ArrayList<>(fields);
        final String output = outputFields.stream().map(outputField -> {
                    if (supportedFields.contains(outputField)) {
                        return fieldValMap.get(outputField);
                    }
                    Matcher csHeaderMatcher = CS_HEADER_REGEX.matcher(outputField);
                    if (csHeaderMatcher.find()) {
                        String headerKey = csHeaderMatcher.group(1);
                        return getCsHeader(request, headerKey);
                    }
                    Matcher scHeaderMatcher = SC_HEADER_REGEX.matcher(outputField);
                    if (scHeaderMatcher.find()) {
                        String headerKey = scHeaderMatcher.group(1);
                        return getScHeader(response, headerKey);
                    }
                    return OMITTED_FIELD;
                })
                .reduce((f1, f2) -> String.join(DELIMITER, f1, f2))
                .orElse("");

        writer.write(correlation, output);
    }

    private String getCsHeader(HttpRequest request, String key) {
        return buildHeaderString(request.getHeaders(), key);
    }

    private String getScHeader(HttpResponse response, String key) {
        return buildHeaderString(response.getHeaders(), key);
    }

    private String buildHeaderString(HttpHeaders httpHeaders, String key) {
        return Optional.of(httpHeaders)
                .map(headers -> httpHeaders.get(key))
                .map(values ->
                        values.stream().reduce(
                                        (v1, v2) ->
                                                String.join(HEADER_DELIMITER, v1, v2))
                                .map(valueStr -> "\"".concat(valueStr).concat("\""))
                                .orElse(OMITTED_FIELD))
                .orElse(OMITTED_FIELD);
    }

    /**
     * Common supported fields
     * @see <a href="https://docs.oracle.com/cd/A97329_03/bi.902/a90500/admin-05.htm#634823">Oracle analytical tool log
     * parsing description</a>
     */
    private enum Field {

        DATE("date"),
        TIME("time"),
        TIME_TAKEN("time-taken"),
        CS_PROTOCOL("cs-protocol"),
        SC_BYTES("sc-bytes"),
        CS_BYTES("cs-bytes"),
        CLIENT_IP("c-ip"),
        SERVER_DNS("s-dns"),
        RESP_STATUS("sc-status"),
        RESP_COMMENT("sc-comment"),
        REQ_METHOD("cs-method"),
        REQ_URI("cs-uri"),
        REQ_URI_STEM("cs-uri-stem"),
        REQ_URI_QUERY("cs-uri-query");

        private final String value;

        Field(String label) {
            this.value = label;
        }

    }

    private List<String> getSupportedFields() {
        return Arrays.stream(Field.values()).map(field -> field.value)
                .collect(Collectors.toList());
    }

    // validate fields, ignore unknown fields
    // if user input fields are empty after filtering, use default fields
    private List<String> getFields(String fieldExpression) {
        List<String> fields = getFieldsFromExpression(fieldExpression);
        if (fields.isEmpty()) {
            return getFieldsFromExpression(DEFAULT_FIELDS);
        }
        return fields;
    }

    private List<String> getFieldsFromExpression(String fieldExpression) {
        List<String> fieldList = Arrays.asList(fieldExpression.split(DELIMITER));
        return fieldList.stream()
                .filter(field -> !field.equals(""))
                .collect(Collectors.toList());
    }

    private void logDirectives(String version, List<String> fields) {
        String date = dateFormatter.format(Instant.now().atZone(timeZone));
        Logger log = LoggerFactory.getLogger(Logbook.class);
        log.trace("#Version: {}", version);
        log.trace("#Date: {}", date);
        log.trace("#Fields: {}", fields);
    }

}
