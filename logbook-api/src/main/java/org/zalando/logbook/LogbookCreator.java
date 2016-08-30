package org.zalando.logbook;

import lombok.Singular;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public final class LogbookCreator {

    LogbookCreator() {
        // package private so we can trick code coverage
    }

    @lombok.Builder(builderClassName = "Builder")
    private static Logbook create(
            @Nullable final Predicate<RawHttpRequest> condition,
            @Singular final List<QueryObfuscator> queryObfuscators,
            @Singular final List<HeaderObfuscator> headerObfuscators,
            @Singular final List<BodyObfuscator> bodyObfuscators,
            @Singular final List<RequestObfuscator> requestObfuscators,
            @Singular final List<ResponseObfuscator> responseObfuscators,
            @Nullable final HttpLogFormatter formatter,
            @Nullable final HttpLogWriter writer) {

        final LogbookFactory factory = LogbookFactory.INSTANCE;

        final QueryObfuscator queryObfuscator = queryObfuscators.stream()
                .reduce(QueryObfuscator::merge)
                .orElse(null);

        final HeaderObfuscator headerObfuscator = headerObfuscators.stream()
                .reduce(HeaderObfuscator::merge)
                .orElse(null);

        final BodyObfuscator bodyObfuscator = bodyObfuscators.stream()
                .reduce(BodyObfuscator::merge)
                .orElse(null);

        final RequestObfuscator requestObfuscator = requestObfuscators.stream()
                .reduce(RequestObfuscator::merge)
                .orElse(null);

        final ResponseObfuscator responseObfuscator = responseObfuscators.stream()
                .reduce(ResponseObfuscator::merge)
                .orElse(null);

        return factory.create(
                condition,
                queryObfuscator,
                headerObfuscator,
                bodyObfuscator,
                requestObfuscator,
                responseObfuscator,
                formatter,
                writer);
    }

}
