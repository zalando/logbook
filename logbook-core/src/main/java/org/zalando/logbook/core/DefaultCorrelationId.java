package org.zalando.logbook.core;

import org.apiguardian.api.API;
import org.zalando.logbook.api.CorrelationId;
import org.zalando.logbook.api.HttpRequest;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static org.apiguardian.api.API.Status.MAINTAINED;

@API(status = MAINTAINED)
public final class DefaultCorrelationId implements CorrelationId {

    @Override
    public String generate(final HttpRequest request) {
        final Random random = ThreadLocalRandom.current();
        // set most significant bit to produce fixed length string
        return Long.toHexString(random.nextLong() | Long.MIN_VALUE);
    }

}
