package org.zalando.logbook.autoconfigure;

import org.zalando.logbook.LogbookCreator;

@FunctionalInterface
public interface LogbookCustomizer {
    void customize(LogbookCreator.Builder builder);
}
