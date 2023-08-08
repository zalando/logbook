package org.zalando.logbook.attributes;

import lombok.EqualsAndHashCode;
import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
@EqualsAndHashCode
public final class NoOpAttributeExtractor implements AttributeExtractor {

}
