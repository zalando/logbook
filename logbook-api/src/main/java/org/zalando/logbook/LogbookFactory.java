package org.zalando.logbook;

import javax.annotation.Nullable;
import java.util.function.Predicate;

import static java.util.ServiceLoader.load;

interface LogbookFactory {

    LogbookFactory INSTANCE = load(LogbookFactory.class).iterator().next();

    Logbook create(
            @Nullable final Predicate<RawHttpRequest> condition,
            @Nullable final QueryObfuscator queryObfuscator,
            @Nullable final HeaderObfuscator headerObfuscator,
            @Nullable final BodyObfuscator bodyObfuscator,
            @Nullable final RequestObfuscator requestObfuscator,
            @Nullable final ResponseObfuscator responseObfuscator,
            @Nullable final HttpLogFormatter formatter,
            @Nullable final HttpLogWriter writer);

}
