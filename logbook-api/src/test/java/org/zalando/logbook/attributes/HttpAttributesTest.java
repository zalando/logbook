package org.zalando.logbook.attributes;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class HttpAttributesTest {

    private final Map<String, Object> mapWithTwoKeys = new HashMap<>();
    private final Map<String, Object> mapWithOneKey = new HashMap<>();

    {
        mapWithTwoKeys.put("key1", "val1");
        mapWithTwoKeys.put("key2", "val2");

        mapWithOneKey.put("key", "val");
    }

    @Test
    void emptyHttpAttributesShouldBeImmutable() {
        final HttpAttributes attributes = HttpAttributes.EMPTY;

        assertThat(attributes).isNotEqualTo(null);
        assertThat(attributes).isEqualTo(Collections.emptyMap());
        assertThat(Collections.emptyMap()).isEqualTo(attributes);

        assertThat(attributes.isEmpty()).isTrue();
        assertThat(attributes).isEqualTo(new HttpAttributes());
        assertThat(attributes.hashCode()).isEqualTo(0);
        assertThat(attributes.toString()).isEqualTo("{}");

        //noinspection DataFlowIssue
        assertThatThrownBy(() -> attributes.put("key", "val"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void singletonHttpAttributesShouldBeImmutable() {
        final Map<String, Object> map1Clone = new HashMap<>(mapWithOneKey);
        final HttpAttributes attributes = HttpAttributes.of("key", "val");

        assertThat(attributes).isEqualTo(map1Clone);
        assertThat(map1Clone).isEqualTo(attributes);

        assertThat(attributes.isEmpty()).isFalse();
        assertThat(attributes).isEqualTo(new HttpAttributes(map1Clone));
        assertThat(attributes.hashCode()).isEqualTo(map1Clone.hashCode());
        assertThat(attributes.toString()).isEqualTo("{key=val}");

        assertThatThrownBy(() -> attributes.put("key", "val"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void arbitraryHttpAttributesShouldBeImmutable() {
        final Map<String, Object> map2Clone = new HashMap<>(mapWithTwoKeys);
        final HttpAttributes attributes = new HttpAttributes(mapWithTwoKeys);

        assertThat(attributes).isEqualTo(map2Clone);
        assertThat(map2Clone).isEqualTo(attributes);

        assertThat(attributes.isEmpty()).isFalse();
        assertThat(attributes).isEqualTo(new HttpAttributes(map2Clone));
        assertThat(attributes.hashCode()).isEqualTo(map2Clone.hashCode());
        assertThat(attributes.toString()).isEqualTo("{key1=val1, key2=val2}");

        assertThatThrownBy(() -> attributes.put("key", "val"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
