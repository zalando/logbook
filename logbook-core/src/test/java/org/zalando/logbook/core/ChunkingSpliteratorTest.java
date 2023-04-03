package org.zalando.logbook.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Spliterator.SIZED;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class ChunkingSpliteratorTest {

    @Test
    void shouldEstimateSizeWithoutTrailingPart() {
        final ChunkingSpliterator unit = new ChunkingSpliterator("Hello", 5, 5);
        assertThat(unit.estimateSize()).isOne();
    }

    @Test
    void shouldEstimateSizeWithTrailingPart() {
        final ChunkingSpliterator unit = new ChunkingSpliterator("Hello World", 5, 5);
        assertThat(unit.estimateSize()).isEqualTo(3);
    }

    @Test
    void shouldNotSupportPartitions() {
        final ChunkingSpliterator unit = new ChunkingSpliterator("", 1, 1);
        assertThat(unit.trySplit()).isNull();
    }

    @Test
    void shouldBeSizedWhenMinEqualToMax() {
        final ChunkingSpliterator unit = new ChunkingSpliterator("Hello", 5, 5);
        assertThat((unit.characteristics() & SIZED)).isNotZero();
    }

    @Test
    void shouldNotBeSizedWhenMinIsNotEqualToMax() {
        final ChunkingSpliterator unit = new ChunkingSpliterator("Hello", 4, 5);
        assertThat((unit.characteristics() & SIZED)).isZero();
    }

    @Test
    void shouldFailWhenMinIsZero() {
        assertThrows(IllegalArgumentException.class, () ->
                new ChunkingSpliterator("whatever", 0, 10));
    }

    @Test
    void shouldFailWhenMaxIsZero() {
        assertThrows(IllegalArgumentException.class, () ->
                new ChunkingSpliterator("whatever", 10, 0));
    }

    @Test
    void shouldFailWhenMinGreaterThanMax() {
        assertThrows(IllegalArgumentException.class, () ->
                new ChunkingSpliterator("whatever", 11, 10));
    }

    @Test
    void shouldSplitAfterSplitCharacter() {
        assertThat(split("12345 67890", 5, 6)).isEqualTo(asList("12345 ", "67890"));
        assertThat(split("12345:67890", 5, 6)).isEqualTo(asList("12345:", "67890"));
        assertThat(split("12345,67890", 5, 6)).isEqualTo(asList("12345,", "67890"));
    }

    @Test
    void shouldSplitAfterSplitCharacterWhenChunkLenghtIsMinimal() {
        assertThat(split("12345 67890", 6, 7)).isEqualTo(asList("12345 ", "67890"));
    }

    @Test
    void shouldSplitOnMaxWhenNoSplitCharacterPresent() {
        assertThat(split("123456 789012", 5, 6)).isEqualTo(asList("123456", " 78901", "2"));
    }

    @Test
    void shouldNotSplitWhenMaxIsEqualToLength() {
        assertThat(split("123 45", 1, 6)).isEqualTo(singletonList("123 45"));
    }

    @Test
    void shouldNotSplitWhenMaxIsGreaterThanLength() {
        assertThat(split("123 45", 1, 10)).isEqualTo(singletonList("123 45"));
    }

    @Test
    void shouldSplitWithMinimalChunkLenOfOne() {
        assertThat(split(" space", 1, 5)).isEqualTo(asList(" ", "space"));
    }

    private static List<String> split(final String string, final int min, final int max) {
        return stream(new ChunkingSpliterator(string, min, max), false).collect(toList());
    }

}
