package org.zalando.logbook.openfeign;

import feign.Request;
import feign.Response;
import lombok.AllArgsConstructor;
import lombok.Generated;
import org.apiguardian.api.API;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Logbook.ResponseProcessingStage;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Example usage:
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
@AllArgsConstructor
public final class FeignLogbookLogger extends feign.Logger {
    private final Logbook logbook;
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
    protected IOException logIOException(String configKey, Level logLevel, IOException ioe, long elapsedTime) {
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
    protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response, long elapsedTime) {
        try {
            // Logbook will consume body stream, making it impossible to read it again
            // read body here and create new response based on byte array instead
            byte[] body = response.body() != null ? ByteStreams.toByteArray(response.body().asInputStream()) : null;

            final HttpResponse httpResponse = RemoteResponse.create(response, body);
            stage.get().process(httpResponse).write();

            // create a copy of response to provide consumed body
            return Response.builder()
                    .status(response.status())
                    .request(response.request())
                    .reason(response.reason())
                    .headers(response.headers())
                    .body(body)
                    .build();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
