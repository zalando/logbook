package org.zalando.logbook;

import lombok.AllArgsConstructor;
import lombok.Getter;
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
import java.util.function.Function;
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
        final byte[] requestBody = request.getBody();
        final byte[] responseBody = response.getBody();
        final FieldParameter fieldParameter = new FieldParameter(startTime, correlation, request, response, requestBody,
                responseBody);

        final Map<String, String> fieldValMap = new HashMap<>();
        for (Field field : Field.values()) {
            fieldValMap.put(field.value, field.getExtraction().apply(fieldParameter));
        }

        final List<String> outputFields = new ArrayList<>(fields);
        final String output = outputFields.stream().map(outputField -> getFieldOutput(outputField, fieldValMap,
                        request, response))
                .reduce((f1, f2) -> String.join(DELIMITER, f1, f2))
                .orElse("");

        writer.write(correlation, output);
    }

    private String getFieldOutput(final String outputField, final Map<String, String> fieldValMap,
                                  final HttpRequest request, final HttpResponse response) {
        if (supportedFields.contains(outputField)) {
            return fieldValMap.get(outputField);
        }
        Matcher csHeaderMatcher = CS_HEADER_REGEX.matcher(outputField);
        if (csHeaderMatcher.find()) {
            final String headerKey = csHeaderMatcher.group(1);
            return getCsHeader(request, headerKey);
        }
        Matcher scHeaderMatcher = SC_HEADER_REGEX.matcher(outputField);
        if (scHeaderMatcher.find()) {
            final String headerKey = scHeaderMatcher.group(1);
            return getScHeader(response, headerKey);
        }
        return OMITTED_FIELD;
    }

    private String getCsHeader(final HttpRequest request, final String key) {
        return buildHeaderString(request.getHeaders(), key);
    }

    private String getScHeader(final HttpResponse response, final String key) {
        return buildHeaderString(response.getHeaders(), key);
    }

    private String buildHeaderString(final HttpHeaders httpHeaders, final String key) {
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
     *
     * @see <a href="https://docs.oracle.com/cd/A97329_03/bi.902/a90500/admin-05.htm#634823">Oracle analytical tool log
     * parsing description</a>
     */
    private enum Field {

        DATE("date", fieldParameter -> DateTimeFormatter.ISO_LOCAL_DATE.format(fieldParameter.getStartTime())),
        TIME("time", fieldParameter -> DateTimeFormatter.ISO_LOCAL_TIME.format(fieldParameter.getStartTime())),
        TIME_TAKEN("time-taken", fieldParameter -> BigDecimal
                .valueOf(fieldParameter.getCorrelation().getDuration().toMillis())
                .divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP)
                .toString()),
        CS_PROTOCOL("cs-protocol", fieldParameter -> fieldParameter.getRequest().getProtocolVersion()),
        SC_BYTES("sc-bytes", fieldParameter -> String.valueOf(fieldParameter.getResponseBody().length)),
        CS_BYTES("cs-bytes", fieldParameter -> String.valueOf(fieldParameter.getRequestBody().length)),
        CLIENT_IP("c-ip", fieldParameter -> fieldParameter.getRequest().getRemote()),
        SERVER_DNS("s-dns", fieldParameter -> fieldParameter.getRequest().getHost()),
        RESP_STATUS("sc-status", fieldParameter -> String.valueOf(fieldParameter.getResponse().getStatus())),
        RESP_COMMENT("sc-comment", fieldParameter -> fieldParameter.getResponse().getReasonPhrase()),
        REQ_METHOD("cs-method", fieldParameter -> fieldParameter.getRequest().getMethod()),
        REQ_URI("cs-uri", fieldParameter -> fieldParameter.getRequest().getRequestUri()),
        REQ_URI_STEM("cs-uri-stem", fieldParameter -> fieldParameter.getRequest().getPath()),
        REQ_URI_QUERY("cs-uri-query", fieldParameter -> {
            if (!"".equals(fieldParameter.getRequest().getQuery())) {
                return "?" + fieldParameter.getRequest().getQuery();
            } else {
                return OMITTED_FIELD;
            }
        });

        private final String value;

        private final Function<FieldParameter, String> extraction;

        Field(String label, Function<FieldParameter, String> extract) {
            this.value = label;
            this.extraction = extract;
        }

        Function<FieldParameter, String> getExtraction() {
            return extraction;
        }
    }

    @Getter
    @AllArgsConstructor
    private static class FieldParameter {
        private final ZonedDateTime startTime;
        private final Correlation correlation;
        private final HttpRequest request;
        private final HttpResponse response;
        private final byte[] requestBody;
        private final byte[] responseBody;
    }

    private List<String> getSupportedFields() {
        return Arrays.stream(Field.values()).map(field -> field.value)
                .collect(Collectors.toList());
    }

    private List<String> getFields(final String fieldExpression) {
        final List<String> fields = getFieldsFromExpression(fieldExpression);
        if (fields.isEmpty()) {
            return getFieldsFromExpression(DEFAULT_FIELDS);
        }
        return fields;
    }

    private List<String> getFieldsFromExpression(final String fieldExpression) {
        final List<String> fieldList = Arrays.asList(fieldExpression.split(DELIMITER));
        return fieldList.stream()
                .filter(field -> !field.equals(""))
                .collect(Collectors.toList());
    }

    private void logDirectives(final String version, final List<String> fields) {
        final String date = DateTimeFormatter.ISO_LOCAL_DATE.format(Instant.now().atZone(timeZone));
        final Logger log = LoggerFactory.getLogger(Logbook.class);
        log.trace("#Version: {}", version);
        log.trace("#Date: {}", date);
        log.trace("#Fields: {}", fields);
    }

}
