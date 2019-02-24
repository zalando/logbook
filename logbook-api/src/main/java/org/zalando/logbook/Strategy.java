package org.zalando.logbook;

import org.apiguardian.api.API;

import java.io.IOException;

import static org.apiguardian.api.API.Status.STABLE;

/**
 * A strategy is glue between {@link Logbook} integrations and the {@link Sink}. The lifecycle of a request-response
 * pair will invoke methods in the following order:
 *
 * <ol>
 *     <li>{@link Strategy#process(HttpRequest)}</li>
 *     <li>{@link Strategy#write(Precorrelation, HttpRequest, Sink)}</li>
 *     <li>{@link Strategy#process(HttpRequest, HttpResponse)}</li>
 *     <li>{@link Strategy#write(Correlation, HttpRequest, HttpResponse, Sink)}</li>
 * </ol>
 *
 * At each of those points in time different options are available, e.g. to defer logging, apply conditions or even
 * modify something.
 *
 * <a href="https://en.wikipedia.org/wiki/Strategy_pattern">Strategy pattern</a>
 */
@API(status = STABLE)
public interface Strategy {

    /**
     * This method is being called right before the request body is being buffered. The primary goal of this method is
     * to decide whether the body should be recorded or not.
     *
     * Defaults to {@link HttpRequest#withBody()}.
     *
     * @see HttpRequest#withBody()
     * @see HttpRequest#withoutBody()
     * @param request the current request
     * @return the given request
     * @throws IOException see {@link HttpRequest#withBody()}
     */
    default HttpRequest process(final HttpRequest request) throws IOException {
        return request.withBody();
    }

    /**
     * This method is being called right after the response body was buffered. The primary goal of this method is to 
     * decide whether and if then how the request is being logged.
     *
     * Options include but are not limited to:
     * 
     * <ul>
     *     <li>Log request immediately.</li>
     *     <li>Defer logging to a later point in time (e.g. when the response becomes available).</li>
     *     <li>Log request conditionally.</li>
     *     <li>Log request without body. (<strong>Performance penalty</strong> for buffering still applies!)</li>
     * </ul>
     *
     *
     * Defaults to delegating to {@link Sink#write(Precorrelation, HttpRequest)}.
     *
     * @see Sink#write(Precorrelation, HttpRequest)
     * @param precorrelation a preliminary {@link Correlation correlation} which provides an id to correlate request
     *                       and response later
     * @param request the current request
     * @param sink the sink to write to, if needed
     * @throws IOException see {@link Sink#write(Precorrelation, HttpRequest)}
     */
    default void write(final Precorrelation precorrelation, final HttpRequest request,
            final Sink sink) throws IOException {
        sink.write(precorrelation, request);
    }

    /**
     * This method is being called right before the response body is being buffered. The primary goal of this method is
     * to decide whether the body should be recorded or not. <strong>Beware</strong> that the response may or may not
     * be a reliable source of information since it was fully processed yet. Any decision whether to buffer the body
     * or not should be made exclusively based on the provided {@link HttpRequest request}.
     * 
     * Defaults to {@link HttpResponse#withBody()}.
     *
     * @see HttpResponse#withBody()
     * @see HttpResponse#withoutBody()
     * @param request the current request
     * @param response the current response
     * @return the given response
     * @throws IOException see {@link HttpResponse#withBody()}
     */
    default HttpResponse process(final HttpRequest request, final HttpResponse response) throws IOException {
        return response.withBody();
    }

    /**
     * This method is being called right after the response body was buffered. The primary goal of this method is to
     * decide whether and if then how the response (and optionally also the request) is being logged.
     * 
     * Options include but are not limited to:
     * 
     * <ul>
     *     <li>Log response immediately.</li>
     *     <li>Log response conditionally.</li>
     *     <li>Log response without body. (<strong>Performance penalty</strong> for buffering still applies!)</li>
     *     <li>Log request now, instead of earlier.</li>
     *     <li>Log request conditionally based on the response.</li>
     *     <li>Log request without body. (<strong>Performance penalty</strong> for buffering still applies!)</li>
     * </ul>
     *
     * Defaults to delegating to {@link Sink#write(Correlation, HttpRequest, HttpResponse)}.
     *
     * @see Sink#write(Correlation, HttpRequest, HttpResponse)
     * @see Sink#writeBoth(Correlation, HttpRequest, HttpResponse)
     * @param correlation a correlation which provides and id (as well as a duration) to correlate request and response
     *                    later
     * @param request the current request
     * @param response the current response
     * @param sink the sink to write to, if needed
     * @throws IOException see {@link Sink#write(Correlation, HttpRequest, HttpResponse)}
     */
    default void write(final Correlation correlation, final HttpRequest request, final HttpResponse response,
            final Sink sink) throws IOException {
        sink.write(correlation, request, response);
    }

}
