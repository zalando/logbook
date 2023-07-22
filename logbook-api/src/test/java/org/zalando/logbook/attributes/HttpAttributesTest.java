package org.zalando.logbook.attributes;

import org.junit.jupiter.api.Test;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class HttpAttributesTest {

    private final Map<String, Object> mapWithTwoKeys = new HashMap<>();
    private final Map<String, Object> mapWithThreeKeys = new HashMap<>();

    {
        mapWithTwoKeys.put("key1", "val1");
        mapWithTwoKeys.put("key2", "val2");

        mapWithThreeKeys.putAll(mapWithTwoKeys);
        mapWithThreeKeys.put("key", "val");
    }

    @Test
    void emptyHttpAttributesShouldBeImmutable() {
        final HttpAttributes attributes = HttpAttributes.EMPTY;

        assertThat(attributes.containsKey("any key")).isFalse();
        assertThat(attributes.containsValue("any value")).isFalse();
        assertThat(attributes.get("any key")).isNull();
        assertThat(attributes.keySet()).isEqualTo(Collections.emptySet());
        assertThat(attributes.values()).isEqualTo(Collections.emptySet());
        assertThat(attributes.entrySet()).isEqualTo(Collections.emptySet());
        assertThat(attributes.isEmpty()).isTrue();
        assertThat(attributes).hasSize(0);

        assertThat(attributes).isEqualTo(HttpAttributes.of());
        assertThat(attributes.hashCode()).isEqualTo(0);

        // These operations don't affect an empty map, so they won't throw any exceptions
        assertThatCode(attributes::clear).doesNotThrowAnyException();
        assertThat(attributes.remove("any key")).isNull();

        assertThatThrownBy(() -> attributes.put("any key", "any value"))
                .isInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(() -> attributes.fluentPut("any key", "any value"))
                .isInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(() -> attributes.putAll(mapWithTwoKeys))
                .isInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(() -> attributes.fluentPutAll(mapWithTwoKeys))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void singletonHttpAttributesShouldBeImmutable() {
        final HttpAttributes attributes = HttpAttributes.of("key", "val");

        assertThat(attributes).containsKey("key");
        assertThat(attributes).containsValue("val");
        assertThat(attributes.get("key")).isEqualTo("val");
        assertThat(attributes.keySet()).containsExactly("key");
        assertThat(attributes.values()).containsExactly("val");
        assertThat(attributes.entrySet()).containsExactly(new SimpleEntry<>("key", "val"));
        assertThat(attributes.isEmpty()).isFalse();
        assertThat(attributes).hasSize(1);

        assertThat(attributes).isEqualTo(new HttpAttributes().fluentPut("key", "val"));
        assertThat(attributes.hashCode()).isEqualTo(Collections.singletonMap("key", "val").hashCode());

        assertThat(attributes.remove("non-existing key")).isNull();

        assertThatThrownBy(() -> attributes.remove("key"))
                .isInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(attributes::clear)
                .isInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(() -> attributes.put("any key", "any value"))
                .isInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(() -> attributes.fluentPut("any key", "any value"))
                .isInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(() -> attributes.putAll(mapWithTwoKeys))
                .isInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(() -> attributes.fluentPutAll(mapWithTwoKeys))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void normalHttpAttributesShouldBeMutable() {
        final HttpAttributes attributes = new HttpAttributes()
                .fluentPutAll(mapWithTwoKeys)
                .fluentPut("key", "val");

        doAssertions(attributes);
    }

    @Test
    void httpAttributesWithMapShouldBeMutable() {
        final HttpAttributes attributes = HttpAttributes.withMap(mapWithThreeKeys);

        doAssertions(attributes);
    }

    @Test
    void httpAttributesShouldNotBeEqualToHashMap() {
        final HttpAttributes attributes = HttpAttributes.EMPTY;
        assertThat(attributes).isNotEqualTo(Collections.emptyMap());
    }

    private void doAssertions(final HttpAttributes attributes) {
        assertThat(attributes).containsKey("key");
        assertThat(attributes).containsValue("val");
        assertThat(attributes.get("key")).isEqualTo("val");
        assertThat(attributes.keySet()).containsExactly("key1", "key2", "key");
        assertThat(attributes.values()).containsExactly("val1", "val2", "val");
        assertThat(attributes.entrySet()).containsExactly(
                new SimpleEntry<>("key1", "val1"), new SimpleEntry<>("key2", "val2"), new SimpleEntry<>("key", "val")
        );
        assertThat(attributes.isEmpty()).isFalse();
        assertThat(attributes).hasSize(3);

        assertThat(attributes).isEqualTo(
                new HttpAttributes()
                        .fluentPut("key", "val")
                        .fluentPut("key1", "val1")
                        .fluentPut("key2", "val2")
        );

        assertThat(attributes.hashCode()).isEqualTo(mapWithThreeKeys.hashCode());

        assertThat(attributes.remove("non-existing key")).isNull();

        assertThat(attributes.remove("key")).isEqualTo("val");

        attributes.clear();
        assertThat(attributes).isEqualTo(HttpAttributes.EMPTY);
    }

}
