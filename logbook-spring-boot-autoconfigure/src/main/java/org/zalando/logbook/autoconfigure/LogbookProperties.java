package org.zalando.logbook.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.apiguardian.api.API;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.zalando.logbook.servlet.FormRequestMode;

import java.util.ArrayList;
import java.util.List;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(status = INTERNAL)
@ConfigurationProperties(prefix = "logbook")
@Getter
public final class LogbookProperties {

    private final List<String> include = new ArrayList<>();
    private final List<String> exclude = new ArrayList<>();
    private final Obfuscate obfuscate = new Obfuscate();
    private final Write write = new Write();
    private final Filter filter = new Filter();

    @Getter
    public static class Obfuscate {
        private final List<String> headers = new ArrayList<>();
        private final List<String> parameters = new ArrayList<>();
        private final List<String> paths = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class Write {
        private int chunkSize;
        private int maxBodySize = -1;
        private boolean interceptorsEnabled = false;
    }

    @Getter
    @Setter
    public static class Filter {
        private FormRequestMode formRequestMode = FormRequestMode.fromProperties();
    }
}
