package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.logbook.attributes.AttributeExtractor;
import org.zalando.logbook.attributes.NoOpAttributeExtractor;
import org.zalando.logbook.autoconfigure.LogbookProperties.ExtractorProperty;
import org.zalando.logbook.core.attributes.CompositeAttributeExtractor;
import org.zalando.logbook.core.attributes.JwtAllMatchingClaimsExtractor;
import org.zalando.logbook.core.attributes.JwtFirstMatchingClaimExtractor;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@LogbookTest
final class NoOpAttributeExtractorTest {

    @Autowired
    private AttributeExtractor attributeExtractor;

    @Test
    void shouldAutowireNoOpAttributeExtractorByDefault() {
        assertThat(attributeExtractor).isEqualTo(new NoOpAttributeExtractor());
    }
}

final class InvalidExtractorTest {
    @Test
    void shouldThrowOnInvalidType() {
        ExtractorProperty extractorProperty = new ExtractorProperty();
        extractorProperty.setType("BadType");
        assertThatThrownBy(() -> extractorProperty.toExtractor(mock(ObjectMapper.class)))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }
}

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@LogbookTest(profiles = "claim-extractor-first-matching")
final class JwtFirstMatchingClaimExtractorTest {

    @Autowired
    private AttributeExtractor attributeExtractor;

    @Autowired
    private JsonMapper mapper;

    @Test
    void shouldAutowireJwtFirstMatchingClaimExtractor() {
        assertThat(attributeExtractor).isEqualTo(
                JwtFirstMatchingClaimExtractor.builder()
                        .jsonMapper(mapper)
                        .claimKey("Principal")
                        .claimNames(Arrays.asList("sub", "subject"))
                        .build()
        );
    }
}

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@LogbookTest(profiles = "claim-extractor-all-matching")
final class JwtAllMatchingClaimsExtractorTest {

    @Autowired
    private AttributeExtractor attributeExtractor;

    @Autowired
    private JsonMapper mapper;

    @Test
    void shouldAutowireJwtAllMatchingClaimsExtractor() {
        assertThat(attributeExtractor).isEqualTo(
                JwtAllMatchingClaimsExtractor.builder()
                        .jsonMapper(mapper)
                        .claimNames(Arrays.asList("iss", "iat"))
                        .build()
        );
    }
}

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@LogbookTest(profiles = "claim-extractor-composite")
final class CompositeAttributeExtractorTest {

    @Autowired
    private AttributeExtractor attributeExtractor;

    @Autowired
    private JsonMapper mapper;

    @Test
    void shouldAutowireCompositeAttributeExtractor() {

        assertThat(attributeExtractor).isEqualTo(
                new CompositeAttributeExtractor(
                        Arrays.asList(
                                JwtFirstMatchingClaimExtractor.builder()
                                        .jsonMapper(mapper)
                                        .claimKey("subject")
                                        .claimNames(Collections.singletonList("sub"))
                                        .build(),
                                JwtAllMatchingClaimsExtractor.builder()
                                        .jsonMapper(mapper)
                                        .claimNames(Arrays.asList("sub", "iat"))
                                        .build()
                        )
                )
        );
    }
}
