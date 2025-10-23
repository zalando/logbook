package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.function.Consumer;

public class NativeEcsLoggingExtension implements BeforeAllCallback, AfterAllCallback {

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) SpringExtension.getApplicationContext(extensionContext);
        resetLoggingSystem(configurableApplicationContext, loggingSystem -> {
            loggingSystem.setLogLevel(null, LogLevel.ERROR); // TODO Will not be loaded through yml file!!!!!!!
            loggingSystem.setLogLevel("org.zalando.logbook.Logbook", LogLevel.TRACE);
        });
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) SpringExtension.getApplicationContext(extensionContext);

        System.setProperty("logging.structured.format.console", "");
        System.setProperty("logging.structured.ecs.service.name", "");
        System.setProperty("logging.structured.ecs.service.node-name", "");

        resetLoggingSystem(configurableApplicationContext, loggingSystem -> {
        });

        System.clearProperty("logging.structured.format.console");
        System.clearProperty("logging.structured.ecs.service.name");
        System.clearProperty("logging.structured.ecs.service.node-name");
    }

    private static void resetLoggingSystem(ConfigurableApplicationContext configurableApplicationContext, Consumer<LoggingSystem> consumer) {
        LoggingSystem loggingSystem = LoggingSystem.get(configurableApplicationContext.getClassLoader());
        loggingSystem.cleanUp();
        loggingSystem.initialize(new LoggingInitializationContext(configurableApplicationContext.getEnvironment()), null, null);
        consumer.accept(loggingSystem);
    }

}
