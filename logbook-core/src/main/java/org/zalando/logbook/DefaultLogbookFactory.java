package org.zalando.logbook;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;


public final class DefaultLogbookFactory implements LogbookFactory {

    @Override
    public Logbook create(
            @Nullable final Predicate<RawHttpRequest> condition,
            @Nullable final QueryObfuscator queryObfuscator,
            @Nullable final HeaderObfuscator headerObfuscator,
            @Nullable final BodyObfuscator bodyObfuscator,
            @Nullable final RequestObfuscator requestObfuscator,
            @Nullable final ResponseObfuscator responseObfuscator,
            @Nullable final HttpLogFormatter formatter,
            @Nullable final HttpLogWriter writer) {


        final HeaderObfuscator header = Optional.ofNullable(headerObfuscator).orElseGet(Obfuscators::authorization);
        final BodyObfuscator body = Optional.ofNullable(bodyObfuscator).orElseGet(BodyObfuscator::none);

        return new DefaultLogbook(
                Optional.ofNullable(condition).orElse($ -> true),
                combine(queryObfuscator, header, body, requestObfuscator),
                combine(header, body, responseObfuscator),
                Optional.ofNullable(formatter).orElseGet(DefaultHttpLogFormatter::new),
                Optional.ofNullable(writer).orElseGet(DefaultHttpLogWriter::new)
        );
    }

    @Nonnull
    private RequestObfuscator combine(
            @Nullable final QueryObfuscator queryObfuscator,
            final HeaderObfuscator headerObfuscator,
            final BodyObfuscator bodyObfuscator,
            @Nullable final RequestObfuscator requestObfuscator) {

        final QueryObfuscator query = Optional.ofNullable(queryObfuscator).orElseGet(Obfuscators::accessToken);

        return RequestObfuscator.merge(
                Optional.ofNullable(requestObfuscator).orElseGet(RequestObfuscator::none),
                request -> new ObfuscatedHttpRequest(request, query, headerObfuscator, bodyObfuscator));
    }

    @Nonnull
    private ResponseObfuscator combine(
            final HeaderObfuscator headerObfuscator,
            final BodyObfuscator bodyObfuscator,
            @Nullable final ResponseObfuscator responseObfuscator) {

        return ResponseObfuscator.merge(
                Optional.ofNullable(responseObfuscator).orElseGet(ResponseObfuscator::none),
                response -> new ObfuscatedHttpResponse(response, headerObfuscator, bodyObfuscator));
    }
}
