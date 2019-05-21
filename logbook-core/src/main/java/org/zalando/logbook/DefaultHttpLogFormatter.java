package org.zalando.logbook;

import static org.apiguardian.api.API.Status.STABLE;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apiguardian.api.API;

@API(status = STABLE)
public final class DefaultHttpLogFormatter implements HttpLogFormatter {

    /**
     * Produces an HTTP-like request in individual lines.
     *
     * @param precorrelation the request correlation
     * @param request the HTTP request 
     * @return a line-separated HTTP request
     * @throws IOException if reading body fails
     */
    
    @Override
    public String format(Precorrelation precorrelation, HttpRequest request) throws IOException {
        final String correlationId = precorrelation.getId();
        
        String body = request.getBodyAsString();

        StringBuilder builder = new StringBuilder(body.length() + 2048);

        builder.append(direction(request));
        builder.append(" Request: ");
        builder.append(correlationId);
        builder.append('\n');
        
        builder.append(request.getMethod());
        builder.append(' ');
        RequestURI.reconstruct(request, builder);
        builder.append(' ');
        builder.append(request.getProtocolVersion());
        builder.append('\n');
        
        writeHeaders(builder, request);

        if (!body.isEmpty()) {
            builder.append('\n');
            builder.append(body);
        } else {
            builder.setLength(builder.length() - 1); // discard last newline
        }
        
        return builder.toString();
    }

    /**
     * Produces an HTTP-like request in individual lines.
     *
     * @param precorrelation the request correlation
     * @return a line-separated HTTP request
     * @throws IOException if reading body fails
     * @see #prepare(Correlation, HttpResponse)
     * @see #format(List)
     * @see StructuredHttpLogFormatter#prepare(Precorrelation, HttpRequest)
     */

    @Override
    public String format(final Correlation correlation, final HttpResponse response) throws IOException {
        final String correlationId = correlation.getId();
        
        String body = response.getBodyAsString();

        StringBuilder builder = new StringBuilder(body.length() + 2048);

        builder.append(direction(response));
        builder.append(" Response: ");
        builder.append(correlationId);
        builder.append("\nDuration: ");
        builder.append(correlation.getDuration().toMillis());
        builder.append(" ms\n");
        
        builder.append(response.getProtocolVersion());
        builder.append(' ');
        builder.append(response.getStatus());
        final String reasonPhrase = response.getReasonPhrase();
        if(reasonPhrase != null) {
            builder.append(' ');
            builder.append(reasonPhrase);
        }
        
        builder.append('\n');

        writeHeaders(builder, response);

        if (!body.isEmpty()) {
            builder.append('\n');
            builder.append(body);
        } else {
            builder.setLength(builder.length() - 1); // discard last newline
        }
        
        return builder.toString();        
    }

    private void writeHeaders(StringBuilder builder, HttpMessage httpMessage) {
        Map<String, List<String>> headers = httpMessage.getHeaders();
        
        if(!headers.isEmpty()) {
            for (Entry<String, List<String>> entry : headers.entrySet()) {
                builder.append(entry.getKey()); 
                builder.append(": ");
                List<String> headerValues = entry.getValue();
                if(!headerValues.isEmpty()) {
                    for(String value : entry.getValue()) {
                        builder.append(value); 
                        builder.append(", ");
                    }
                    builder.setLength(builder.length() - 2); // discard last comma
                }
                builder.append('\n');
            }
        }
    }

    private String direction(final HttpMessage request) {
        return request.getOrigin() == Origin.REMOTE ? "Incoming" : "Outgoing";
    }
}
