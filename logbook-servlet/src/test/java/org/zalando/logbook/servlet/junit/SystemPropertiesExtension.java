package org.zalando.logbook.servlet.junit;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Properties;

public final class SystemPropertiesExtension
        implements Extension, BeforeEachCallback, AfterEachCallback {

    private final Properties original = new Properties();

    @Override
    public void beforeEach(final ExtensionContext context) throws Exception {
        original.putAll(System.getProperties());
    }

    @Override
    public void afterEach(final ExtensionContext context) throws Exception {
        final Properties properties = System.getProperties();
        properties.clear();
        properties.putAll(original);
    }

}
