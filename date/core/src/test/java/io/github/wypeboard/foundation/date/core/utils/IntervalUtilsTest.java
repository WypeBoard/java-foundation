package io.github.wypeboard.foundation.date.core.utils;

import io.github.wypeboard.foundation.date.core.domain.Interval;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("IntervalUtils")
class IntervalUtilsTest {

    private static final Instant T2024_01_01 = Instant.parse("2024-01-01T00:00:00Z");
    private static final Instant T2024_03_01 = Instant.parse("2024-03-01T00:00:00Z");
    private static final Instant T2024_06_01 = Instant.parse("2024-06-01T00:00:00Z");
    private static final Instant T2024_09_01 = Instant.parse("2024-09-01T00:00:00Z");
    private static final Instant T2024_12_01 = Instant.parse("2024-12-01T00:00:00Z");
    private static final Instant T2025_01_01 = Instant.parse("2025-01-01T00:00:00Z");
    
    @Test
    @DisplayName("normalize: empty collection returns empty list")
    void normalizeEmptyReturnsEmpty() {
        assertEquals(List.of(), IntervalUtils.normalize(List.of()));
    }

    @Test
    @DisplayName("normalize: null collection returns empty list")
    void normalizeNullReturnsEmpty() {
        assertEquals(List.of(), IntervalUtils.normalize(null));
    }

    @Test
    @DisplayName("normalize: single interval returns itself")
    void normalizeSingleInterval() {
        Interval interval = new Interval(T2024_01_01, T2024_06_01);
        List<Interval> result = IntervalUtils.normalize(List.of(interval));
        assertEquals(1, result.size());
        assertEquals(interval, result.getFirst());
    }

    @Test
    @DisplayName("normalize: overlapping intervals are merged")
    void normalizeOverlappingIntervals() {
        // [T1--T3] overlaps [T2--T4] → [T1--T4]
        List<Interval> result = IntervalUtils.normalize(List.of(
                new Interval(T2024_01_01, T2024_06_01),
                new Interval(T2024_03_01, T2024_09_01)
        ));
        assertEquals(1, result.size());
        assertEquals(new Interval(T2024_01_01, T2024_09_01), result.getFirst());
    }

    @Test
    @DisplayName("normalize: adjacent intervals are merged")
    void normalizeAdjacentIntervals() {
        // [T1--T2] adjacent [T2--T3] → [T1--T3]
        List<Interval> result = IntervalUtils.normalize(List.of(
                new Interval(T2024_01_01, T2024_03_01),
                new Interval(T2024_03_01, T2024_06_01)
        ));
        assertEquals(1, result.size());
        assertEquals(new Interval(T2024_01_01, T2024_06_01), result.getFirst());
    }

    @Test
    @DisplayName("normalize: non-overlapping intervals remain separate")
    void normalizeNonOverlappingRemainSeparate() {
        // [T1--T2] gap [T3--T4] → two intervals unchanged
        List<Interval> result = IntervalUtils.normalize(List.of(
                new Interval(T2024_01_01, T2024_03_01),
                new Interval(T2024_06_01, T2024_09_01)
        ));
        assertEquals(2, result.size());
        assertEquals(new Interval(T2024_01_01, T2024_03_01), result.get(0));
        assertEquals(new Interval(T2024_06_01, T2024_09_01), result.get(1));
    }

    @Test
    @DisplayName("normalize: multiple overlapping collapse into one")
    void normalizeMultipleOverlappingCollapseIntoOne() {
        // [T1--T3] [T2--T4] [T3--T5] → [T1--T5]
        List<Interval> result = IntervalUtils.normalize(List.of(
                new Interval(T2024_01_01, T2024_06_01),
                new Interval(T2024_03_01, T2024_09_01),
                new Interval(T2024_06_01, T2024_12_01)
        ));
        assertEquals(1, result.size());
        assertEquals(new Interval(T2024_01_01, T2024_12_01), result.getFirst());
    }

    @Test
    @DisplayName("normalize: result is unmodifiable")
    void normalizeResultIsUnmodifiable() {
        List<Interval> result = IntervalUtils.normalize(List.of(new Interval(T2024_01_01, T2024_03_01)));
        assertThrows(UnsupportedOperationException.class, () -> result.add(new Interval(T2024_06_01, T2024_09_01)));
    }

    @Test
    @DisplayName("atomize: empty collection returns empty")
    void atomizeEmptyReturnsEmpty() {
        assertEquals(List.of(), IntervalUtils.atomize(List.of()));
    }

    @Test
    @DisplayName("atomize: single interval returns itself")
    void atomizeSingleInterval() {
        List<Interval> result = IntervalUtils.atomize(List.of(new Interval(T2024_01_01, T2024_06_01)));
        assertEquals(1, result.size());
        assertEquals(new Interval(T2024_01_01, T2024_06_01), result.getFirst());
    }

    @Test
    @DisplayName("atomize: overlapping intervals split at boundaries")
    void atomizeOverlappingIntervalsSplitAtBoundaries() {
        // [T1--T3] [T2--T4] → [T1--T2] [T2--T3] [T3--T4]
        List<Interval> result = IntervalUtils.atomize(List.of(
                new Interval(T2024_01_01, T2024_06_01),
                new Interval(T2024_03_01, T2024_09_01)
        ));
        assertEquals(3, result.size());
        assertEquals(new Interval(T2024_01_01, T2024_03_01), result.get(0));
        assertEquals(new Interval(T2024_03_01, T2024_06_01), result.get(1));
        assertEquals(new Interval(T2024_06_01, T2024_09_01), result.get(2));
    }

    @Test
    @DisplayName("atomize: result is unmodifiable")
    void atomizeResultIsUnmodifiable() {
        List<Interval> result = IntervalUtils.atomize(List.of(new Interval(T2024_01_01, T2024_03_01)));
        assertThrows(UnsupportedOperationException.class, () -> result.add(new Interval(T2024_06_01, T2024_09_01)));
    }

    @Test
    @DisplayName("overlappingWith: returns intervals that overlap target")
    void overlappingWithReturnsMatches() {
        Interval target = new Interval(T2024_03_01, T2024_09_01);
        List<Interval> candidates = List.of(
                new Interval(T2024_01_01, T2024_06_01), // overlaps
                new Interval(T2024_06_01, T2024_12_01), // overlaps at boundary
                new Interval(T2024_12_01, T2025_01_01)  // does not overlap
        );
        List<Interval> result = IntervalUtils.overlappingWith(candidates, target);
        assertEquals(2, result.size());
        assertTrue(result.contains(new Interval(T2024_01_01, T2024_06_01)));
        assertTrue(result.contains(new Interval(T2024_06_01, T2024_12_01)));
    }

    @Test
    @DisplayName("overlappingWith: returns empty when none overlap")
    void overlappingWithReturnsEmptyWhenNoneOverlap() {
        Interval target = new Interval(T2024_12_01, T2025_01_01);
        List<Interval> result = IntervalUtils.overlappingWith(
                List.of(new Interval(T2024_01_01, T2024_03_01), new Interval(T2024_03_01, T2024_06_01)),
                target
        );
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("overlappingWith: throws on null target")
    void overlappingWithThrowsOnNullTarget() {
        assertThrows(NullPointerException.class,
                () -> IntervalUtils.overlappingWith(List.of(), null));
    }

    @Test
    @DisplayName("containing: returns intervals that contain the instant")
    void containingReturnsMatches() {
        List<Interval> intervals = List.of(
                new Interval(T2024_01_01, T2024_06_01),
                new Interval(T2024_03_01, T2024_09_01),
                new Interval(T2024_09_01, T2024_12_01)
        );
        List<Interval> result = IntervalUtils.containing(intervals, T2024_03_01);
        assertEquals(2, result.size());
        assertTrue(result.contains(new Interval(T2024_01_01, T2024_06_01)));
        assertTrue(result.contains(new Interval(T2024_03_01, T2024_09_01)));
    }

    @Test
    @DisplayName("containing: returns empty when none contain the instant")
    void containingReturnsEmptyWhenNoneMatch() {
        List<Interval> result = IntervalUtils.containing(
                List.of(new Interval(T2024_01_01, T2024_03_01)),
                T2024_12_01
        );
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("containing: throws on null instant")
    void containingThrowsOnNullInstant() {
        assertThrows(NullPointerException.class,
                () -> IntervalUtils.containing(List.of(), null));
    }

    @Test
    @DisplayName("gap: returns gap interval between non-overlapping intervals")
    void gapReturnsBetweenNonOverlapping() {
        // [T1--T2] gap [T4--T5] → gap is [T2--T4]
        Optional<Interval> gap = IntervalUtils.gap(
                new Interval(T2024_01_01, T2024_03_01),
                new Interval(T2024_09_01, T2024_12_01)
        );
        assertTrue(gap.isPresent());
        assertEquals(new Interval(T2024_03_01, T2024_09_01), gap.get());
    }

    @Test
    @DisplayName("gap: returns empty when intervals overlap")
    void gapReturnsEmptyWhenOverlapping() {
        Optional<Interval> gap = IntervalUtils.gap(
                new Interval(T2024_01_01, T2024_06_01),
                new Interval(T2024_03_01, T2024_09_01)
        );
        assertTrue(gap.isEmpty());
    }

    @Test
    @DisplayName("gap: returns empty when intervals are adjacent")
    void gapReturnsEmptyWhenAdjacent() {
        Optional<Interval> gap = IntervalUtils.gap(
                new Interval(T2024_01_01, T2024_03_01),
                new Interval(T2024_03_01, T2024_06_01)
        );
        assertTrue(gap.isEmpty());
    }

    @Test
    @DisplayName("gap: is commutative — order of arguments does not matter")
    void gapIsCommutative() {
        Interval a = new Interval(T2024_01_01, T2024_03_01);
        Interval b = new Interval(T2024_09_01, T2024_12_01);
        assertEquals(IntervalUtils.gap(a, b), IntervalUtils.gap(b, a));
    }
}