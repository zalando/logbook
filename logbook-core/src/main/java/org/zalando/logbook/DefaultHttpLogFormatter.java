package org.zalando.logbook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.zalando.logbook.Origin.REMOTE;

public final class DefaultHttpLogFormatter implements HttpLogFormatter {

    private static final Map<String, String> REASON_PHRASES;

    static {
        final Map<String, String> phrases = new HashMap<>();

        phrases.put("100", "Continue");
        phrases.put("101", "Switching Protocols");
        phrases.put("102", "Processing");

        phrases.put("200", "OK");
        phrases.put("201", "Created");
        phrases.put("202", "Accepted");
        phrases.put("203", "Non-Authoritative Information");
        phrases.put("204", "No Content");
        phrases.put("205", "Reset Content");
        phrases.put("206", "Partial Content");
        phrases.put("207", "Multi-Status");
        phrases.put("208", "Already Reported");
        phrases.put("226", "IM Used");

        phrases.put("300", "Multiple Choices");
        phrases.put("301", "Moved Permanently");
        phrases.put("302", "Found");
        phrases.put("303", "See Other");
        phrases.put("304", "Not Modified");
        phrases.put("305", "Use Proxy");
        phrases.put("306", "Switch Proxy");
        phrases.put("307", "Temporary Redirect");
        phrases.put("308", "Permanent Redirect");

        phrases.put("400", "Bad Request");
        phrases.put("401", "Unauthorized");
        phrases.put("402", "Payment Required");
        phrases.put("403", "Forbidden");
        phrases.put("404", "Not Found");
        phrases.put("405", "Method Not Allowed");
        phrases.put("406", "Not Acceptable");
        phrases.put("407", "Proxy Authentication Required");
        phrases.put("408", "Request Timeout");
        phrases.put("409", "Conflict");
        phrases.put("410", "Gone");
        phrases.put("411", "Length Required");
        phrases.put("412", "Precondition Failed");
        phrases.put("413", "Payload Too Large");
        phrases.put("414", "URI Too Long");
        phrases.put("415", "Unsupported Media Type");
        phrases.put("416", "Requested Range Not Satisfiable");
        phrases.put("417", "Expectation Failed");
        phrases.put("418", "I'm a teapot");
        phrases.put("421", "Misdirected Request");
        phrases.put("422", "Unprocessable Entity");
        phrases.put("423", "Locked");
        phrases.put("424", "Failed Dependency");
        phrases.put("426", "Upgrade Required");
        phrases.put("428", "Precondition Required");
        phrases.put("429", "Too Many Requests");
        phrases.put("431", "Request Header Fields Too Large");
        phrases.put("444", "Connection Closed Without Response");
        phrases.put("451", "Unavailable For Legal Reasons");
        phrases.put("499", "Client Closed Request");

        phrases.put("500", "Internal Server Error");
        phrases.put("501", "Not Implemented");
        phrases.put("502", "Bad Gateway");
        phrases.put("503", "Service Unavailable");
        phrases.put("504", "Gateway Timeout");
        phrases.put("505", "HTTP Version Not Supported");
        phrases.put("506", "Variant Also Negotiates");
        phrases.put("507", "Insufficient Storage");
        phrases.put("508", "Loop Detected");
        phrases.put("510", "Not Extended");
        phrases.put("511", "Network Authentication Required");
        phrases.put("599", "Network Connect Timeout Error");

        REASON_PHRASES = Collections.unmodifiableMap(phrases);
    }

    @Override
    public String format(final Precorrelation<HttpRequest> precorrelation) throws IOException {
        return format(prepare(precorrelation));
    }

    /**
     * Produces an HTTP-like request in individual lines.
     *
     * @param precorrelation the request correlation
     * @return a line-separated HTTP request
     * @throws IOException
     * @see #prepare(Correlation)
     * @see #format(List)
     * @see JsonHttpLogFormatter#prepare(Precorrelation)
     */
    public List<String> prepare(final Precorrelation<HttpRequest> precorrelation) throws IOException {
        final HttpRequest request = precorrelation.getRequest();
        final String requestLine = String.format("%s %s %s", request.getMethod(), request.getRequestUri(),
                request.getProtocolVersion());
        return prepare(request, "Request", precorrelation.getId(), requestLine);
    }

    @Override
    public String format(final Correlation<HttpRequest, HttpResponse> correlation) throws IOException {
        return format(prepare(correlation));
    }

    /**
     * Produces an HTTP-like response in individual lines.
     * <p>
     * Pr@param correlation the response correlation
     *
     * @return a line-separated HTTP response
     * @throws IOException
     * @see #prepare(Precorrelation)
     * @see #format(List)
     * @see JsonHttpLogFormatter#prepare(Correlation)
     */
    public List<String> prepare(final Correlation<HttpRequest, HttpResponse> correlation) throws IOException {
        final HttpResponse response = correlation.getResponse();
        final int status = response.getStatus();
        final String reasonPhrase = REASON_PHRASES.getOrDefault(Integer.toString(status), "");
        final String statusLine = String.format("%s %d %s", response.getProtocolVersion(), status, reasonPhrase).trim();
        return prepare(response, "Response", correlation.getId(),
                "Duration: " + correlation.getDuration().toMillis() + " ms", statusLine);
    }

    private <H extends HttpMessage> List<String> prepare(final H message, final String type,
            final String correlationId, final String... prefixes) throws IOException {
        final List<String> lines = new ArrayList<>();

        lines.add(direction(message) + " " + type + ": " + correlationId);
        Collections.addAll(lines, prefixes);
        lines.addAll(formatHeaders(message));

        final String body = message.getBodyAsString();

        if (!body.isEmpty()) {
            lines.add("");
            lines.add(body);
        }

        return lines;
    }

    private String direction(final HttpMessage request) {
        return request.getOrigin() == REMOTE ? "Incoming" : "Outgoing";
    }

    private List<String> formatHeaders(final HttpMessage message) {
        return message.getHeaders().entrySet().stream()
                .collect(toMap(Map.Entry::getKey, this::formatHeaderValues))
                .entrySet().stream()
                .map(this::formatHeader)
                .collect(toList());
    }

    private String formatHeaderValues(final Map.Entry<String, List<String>> entry) {
        return entry.getValue().stream().collect(joining(", "));
    }

    private String formatHeader(final Map.Entry<String, String> entry) {
        return String.format("%s: %s", entry.getKey(), entry.getValue());
    }

    /**
     * Renders an HTTP-like message into a printable string.
     *
     * @param lines lines of an HTTP message
     * @return the whole message as a single string, separated by new lines
     * @see #prepare(Precorrelation)
     * @see #prepare(Correlation)
     * @see JsonHttpLogFormatter#format(Map)
     */
    public String format(final List<String> lines) {
        return lines.stream().collect(joining("\n"));
    }

}
