package org.zalando.logbook.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import org.apiguardian.api.API;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.zalando.logbook.attributes.AttributeExtractor;
import org.zalando.logbook.core.attributes.JwtAllMatchingClaimsExtractor;
import org.zalando.logbook.core.attributes.JwtAllMatchingClaimsExtractorJackson2;
import org.zalando.logbook.core.attributes.JwtFirstMatchingClaimExtractor;
import org.zalando.logbook.core.attributes.JwtFirstMatchingClaimExtractorJackson2;
import org.zalando.logbook.servlet.FormRequestMode;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.json.JsonMapper;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(status = INTERNAL)
@ConfigurationProperties(prefix = "logbook")
@Getter
public final class LogbookProperties {

    private final List<String> include = new ArrayList<>();
    private final List<String> exclude = new ArrayList<>();
    private final PredicateProperties predicate = new PredicateProperties();
    private final Obfuscate obfuscate = new Obfuscate();
    private final Write write = new Write();
    private final Filter filter = new Filter();
    private final List<ExtractorProperty> attributeExtractors = new ArrayList<>();
    private final Ecs ecs = new Ecs();

    @Getter
    @Setter
    public static class Obfuscate {
        private final List<String> headers = new ArrayList<>();
        private final List<String> parameters = new ArrayList<>();
        private final List<String> paths = new ArrayList<>();
        private final List<String> jsonBodyFields = new ArrayList<>();
        private String replacement = "XXX";
    }

    @Getter
    @Setter
    public static class Write {
        private int chunkSize;
        private int maxBodySize = -1;
    }

    @Getter
    @Setter
    public static class Filter {
        private FormRequestMode formRequestMode = FormRequestMode.fromProperties();
    }

    @Getter
    @Setter
    // A hack to not have JaCoCo complain about missing test coverage for this method as jackson 3 classes are not in the classpath during tests
    @Generated
    public static class ExtractorProperty {
        @Nonnull
        private String type;
        @Nullable
        private List<String> claimNames;
        @Nullable
        private String claimKey;

        public AttributeExtractor toExtractor(final Object mapper) {
            // Detect which Jackson version is being used based on ObjectMapper class
            final boolean isJackson3 = mapper.getClass().getName().startsWith("tools.jackson.");

            switch (type) {
                case "JwtFirstMatchingClaimExtractor":
                    if (isJackson3) {
                        return JwtFirstMatchingClaimExtractor.builder()
                                .jsonMapper((JsonMapper) mapper)
                                .claimNames(claimNames)
                                .claimKey(claimKey)
                                .build();
                    } else {
                        return JwtFirstMatchingClaimExtractorJackson2.builder()
                                .objectMapper((ObjectMapper) mapper)
                                .claimNames(claimNames)
                                .claimKey(claimKey)
                                .build();
                    }
                case "JwtAllMatchingClaimsExtractor":
                    if (isJackson3) {
                        return JwtAllMatchingClaimsExtractor.builder()
                                .jsonMapper((JsonMapper) mapper)
                                .claimNames(claimNames)
                                .build();
                    } else {
                        return JwtAllMatchingClaimsExtractorJackson2.builder()
                                .objectMapper((ObjectMapper) mapper)
                                .claimNames(claimNames)
                                .build();
                    }
                default:
                    throw new IllegalArgumentException("Unknown AttributeExtractor type: " + type);
            }
        }
    }

    @Getter
    @Setter
    public static class PredicateProperties {
        private List<LogbookPredicate> include = new ArrayList<>();
        private List<LogbookPredicate> exclude = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class LogbookPredicate {
        private String path;
        private List<String> methods = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class Ecs {

        private String customNamespacePrefix = "logbook";

    }

}
