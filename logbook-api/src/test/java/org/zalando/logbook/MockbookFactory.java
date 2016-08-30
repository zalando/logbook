package org.zalando.logbook;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public final class MockbookFactory implements LogbookFactory {

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

        return new Mockbook(
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
