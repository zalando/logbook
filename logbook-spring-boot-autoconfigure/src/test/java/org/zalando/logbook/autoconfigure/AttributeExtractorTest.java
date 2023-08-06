package org.zalando.logbook.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.logbook.attributes.AttributeExtractor;
import org.zalando.logbook.attributes.CompositeAttributeExtractor;
import org.zalando.logbook.attributes.JwtAllMatchingClaimsExtractor;
import org.zalando.logbook.attributes.JwtFirstMatchingClaimExtractor;
import org.zalando.logbook.attributes.NoOpAttributeExtractor;
import org.zalando.logbook.autoconfigure.LogbookProperties.ExtractorProperty;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        assertThat(attributeExtractor).isInstanceOf(NoOpAttributeExtractor.class);
    }
}

final class InvalidExtractorTest {
    @Test
    void shouldThrowOnInvalidType() {
        ExtractorProperty extractorProperty = new ExtractorProperty();
        extractorProperty.setType("BadType");
        assertThatThrownBy(() -> extractorProperty.toExtractor(mock()))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }
}

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@LogbookTest(profiles = "claim-extractor-first-matching")
final class JwtFirstMatchingClaimExtractorTest {

    @Autowired
    private AttributeExtractor attributeExtractor;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldAutowireNoOpAttributeExtractorByDefault() {
        assertThat(attributeExtractor).isInstanceOf(JwtFirstMatchingClaimExtractor.class);

        final JwtFirstMatchingClaimExtractor jwtFirstMatchingClaimExtractor = (JwtFirstMatchingClaimExtractor) attributeExtractor;
        assertThat(jwtFirstMatchingClaimExtractor.getClaimKey()).isEqualTo("Principal");
        assertThat(jwtFirstMatchingClaimExtractor.getObjectMapper()).isEqualTo(objectMapper);
        assertThat(jwtFirstMatchingClaimExtractor.getClaimNames()).isEqualTo(Arrays.asList("sub", "subject"));
        assertThat(jwtFirstMatchingClaimExtractor.isExceptionLogged()).isEqualTo(true);
    }
}

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@LogbookTest(profiles = "claim-extractor-all-matching")
final class JwtAllMatchingClaimsExtractorTest {

    @Autowired
    private AttributeExtractor attributeExtractor;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldAutowireNoOpAttributeExtractorByDefault() {
        assertThat(attributeExtractor).isInstanceOf(JwtAllMatchingClaimsExtractor.class);

        final JwtAllMatchingClaimsExtractor jwtAllMatchingClaimsExtractor = (JwtAllMatchingClaimsExtractor) attributeExtractor;
        assertThat(jwtAllMatchingClaimsExtractor.getObjectMapper()).isEqualTo(objectMapper);
        assertThat(jwtAllMatchingClaimsExtractor.getClaimNames()).isEqualTo(Arrays.asList("iss", "iat"));
        assertThat(jwtAllMatchingClaimsExtractor.isExceptionLogged()).isEqualTo(false);
    }
}

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@LogbookTest(profiles = "claim-extractor-composite")
final class CompositeAttributeExtractorTest {

    @Autowired
    private AttributeExtractor attributeExtractor;

    @Autowired
    private ObjectMapper objectMapper;

    @SuppressWarnings("unchecked")
    @Test
    void shouldAutowireNoOpAttributeExtractorByDefault() throws Exception {
        assertThat(attributeExtractor).isInstanceOf(CompositeAttributeExtractor.class);

        final Field field = CompositeAttributeExtractor.class.getDeclaredField("attributeExtractors");
        field.setAccessible(true);
        final List<AttributeExtractor> attributeExtractors =
                (List<AttributeExtractor>) field.get(attributeExtractor);

        assertThat(attributeExtractors).hasSize(2);
        final AttributeExtractor extractor0 = attributeExtractors.get(0);
        final AttributeExtractor extractor1 = attributeExtractors.get(1);

        assertThat(extractor0).isInstanceOf(JwtFirstMatchingClaimExtractor.class);
        final JwtFirstMatchingClaimExtractor jwtFirstMatchingClaimExtractor = (JwtFirstMatchingClaimExtractor) extractor0;
        assertThat(jwtFirstMatchingClaimExtractor.getClaimKey()).isEqualTo("subject");
        assertThat(jwtFirstMatchingClaimExtractor.getObjectMapper()).isEqualTo(objectMapper);
        assertThat(jwtFirstMatchingClaimExtractor.getClaimNames()).isEqualTo(Collections.singletonList("sub"));
        assertThat(jwtFirstMatchingClaimExtractor.isExceptionLogged()).isEqualTo(true);

        assertThat(extractor1).isInstanceOf(JwtAllMatchingClaimsExtractor.class);
        final JwtAllMatchingClaimsExtractor jwtAllMatchingClaimsExtractor = (JwtAllMatchingClaimsExtractor) extractor1;
        assertThat(jwtAllMatchingClaimsExtractor.getObjectMapper()).isEqualTo(objectMapper);
        assertThat(jwtAllMatchingClaimsExtractor.getClaimNames()).isEqualTo(Arrays.asList("sub", "iat"));
        assertThat(jwtAllMatchingClaimsExtractor.isExceptionLogged()).isEqualTo(false);
    }
}
