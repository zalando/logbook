package org.zalando.logbook.openfeign;

import static org.zalando.logbook.openfeign.Utils.ensureClosed;

import feign.Request;
import feign.Response;

import lombok.Generated;

import org.apiguardian.api.API;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Logbook.ResponseProcessingStage;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.SequenceInputStream;
import java.io.UncheckedIOException;

/**
 * Example usage:
 *
 * <pre>{@code
 * Logbook logbook = ...;
 * FeignLogbookLogger interceptor = new FeignLogbookLogger(logbook);
 * client = Feign.builder()
 *         ...
 *         .logger(interceptor)
 *         .logLevel(Logger.Level.FULL)
 *         ...;
 * }</pre>
 */
@API(status = API.Status.EXPERIMENTAL)
public final class FeignLogbookLogger extends feign.Logger {
    private final Logbook logbook;
    private final int maxResponseBodySize;

    public FeignLogbookLogger(final Logbook logbook) {
        this.logbook = logbook;
        this.maxResponseBodySize = -1;
    }

    /**
     * Limits response body buffering for logging. Bodies exceeding the limit are omitted from the
     * log, while the complete body remains available to the caller as a stream.
     *
     * @param logbook the logbook to use
     * @param maxResponseBodySize maximum body size in bytes; zero disables response body buffering
     * @throws IllegalArgumentException if the limit is negative
     */
    public FeignLogbookLogger(final Logbook logbook, final int maxResponseBodySize) {
        if (maxResponseBodySize < 0) {
            throw new IllegalArgumentException("maxResponseBodySize must not be negative");
        }
        this.logbook = logbook;
        this.maxResponseBodySize = maxResponseBodySize;
    }

    // Feign is blocking, so there is no context switch between request and response
    private final ThreadLocal<ResponseProcessingStage> stage = new ThreadLocal<>();

    @Override
    @Generated
    // HACK: JaCoCo ignores a code with "*Generated*" annotation
    // this method is a rudiment (not called anywhere), and shouldn't be covered
    protected void log(String configKey, String format, Object... args) {
        /* no-op, logging is delegated to logbook */
    }

    @Override
    protected void logRetry(String configKey, Level logLevel) {
        /* no-op, logging is delegated to logbook */
    }

    @Override
    protected IOException logIOException(
            String configKey, Level logLevel, IOException ioe, long elapsedTime) {
        /* no-op, logging is delegated to logbook */
        return ioe;
    }

    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
        final HttpRequest httpRequest = LocalRequest.create(request);
        try {
            ResponseProcessingStage processingStage = logbook.process(httpRequest).write();
            stage.set(processingStage);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    protected Response logAndRebufferResponse(
            String configKey, Level logLevel, Response response, long elapsedTime) {
        try {
            final Response.Body responseBody = response.body();
            if (responseBody != null
                    && maxResponseBodySize >= 0
                    && (maxResponseBodySize == 0
                            || responseBody.length() != null
                                    && responseBody.length() > maxResponseBodySize)) {
                stage.get().process(RemoteResponse.create(response, null)).write();
                return response;
            }

            byte[] body = null;
            if (responseBody != null) {
                InputStream bodyInputStream = null;
                boolean streaming = false;
                try {
                    bodyInputStream = responseBody.asInputStream();
                    if (maxResponseBodySize < 0) {
                        body = ByteStreams.toByteArray(bodyInputStream);
                    } else {
                        final PushbackInputStream input = new PushbackInputStream(bodyInputStream);
                        body = input.readNBytes(maxResponseBodySize);
                        final int next = input.read();
                        if (next != -1) {
                            input.unread(next);
                            stage.get().process(RemoteResponse.create(response, null)).write();
                            final InputStream replay =
                                    new SequenceInputStream(new ByteArrayInputStream(body), input);
                            final InputStream stream =
                                    new FilterInputStream(replay) {
                                        @Override
                                        public void close() throws IOException {
                                            try {
                                                super.close();
                                            } finally {
                                                responseBody.close();
                                            }
                                        }
                                    };
                            final Response result =
                                    response.toBuilder()
                                            .body(stream, responseBody.length())
                                            .build();
                            streaming = true;
                            return result;
                        }
                    }
                } finally {
                    if (!streaming) {
                        ensureClosed(bodyInputStream);
                        ensureClosed(responseBody);
                    }
                }
            }

            final HttpResponse httpResponse = RemoteResponse.create(response, body);
            stage.get().process(httpResponse).write();

            // create a copy of response to provide consumed body
            return response.toBuilder().body(body).build();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            stage.remove();
        }
    }
}
