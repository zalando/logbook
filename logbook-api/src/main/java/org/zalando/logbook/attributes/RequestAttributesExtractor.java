package org.zalando.logbook.attributes;

import org.apiguardian.api.API;
import org.zalando.logbook.HttpRequest;

import javax.annotation.Nonnull;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
@FunctionalInterface
public interface RequestAttributesExtractor {

    @Nonnull
    HttpAttributes extract(final HttpRequest request) throws Exception;

}
