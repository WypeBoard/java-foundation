package io.github.wypeboard.foundation.date.core.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Interval")
class IntervalTest {

    private static final Instant T2024_01_01 = Instant.parse("2024-01-01T00:00:00Z");
    private static final Instant T2024_06_01 = Instant.parse("2024-06-01T00:00:00Z");
    private static final Instant T2024_12_31 = Instant.parse("2024-12-31T00:00:00Z");

    @Test
    @DisplayName("constructs with valid from/to")
    void constructsWithValidRange() {
        Interval interval = new Interval(T2024_01_01, T2024_12_31);
        assertEquals(T2024_01_01, interval.from());
        assertEquals(T2024_12_31, interval.to());
    }

    @Test
    @DisplayName("constructs when from equals to (point in time)")
    void constructsWhenFromEqualsTo() {
        assertDoesNotThrow(() -> new Interval(T2024_01_01, T2024_01_01));
    }

    @Test
    @DisplayName("throws when to is before from")
    void throwsWhenToBeforeFrom() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Interval(T2024_12_31, T2024_01_01)
        );
        assertTrue(ex.getMessage().contains("cannot be before"));
    }

    @Test
    @DisplayName("throws when from is null")
    void throwsWhenFromIsNull() {
        assertThrows(NullPointerException.class, () -> new Interval(null, T2024_12_31));
    }

    @Test
    @DisplayName("throws when to is null")
    void throwsWhenToIsNull() {
        assertThrows(NullPointerException.class, () -> new Interval(T2024_01_01, null));
    }

    @Test
    @DisplayName("open() with null from uses Instant.MIN")
    void openWithNullFromUsesMin() {
        Interval interval = Interval.open(null, T2024_12_31);
        assertEquals(Instant.MIN, interval.from());
        assertEquals(T2024_12_31, interval.to());
    }

    @Test
    @DisplayName("open() with null to uses Instant.MAX")
    void openWithNullToUsesMax() {
        Interval interval = Interval.open(T2024_01_01, null);
        assertEquals(T2024_01_01, interval.from());
        assertEquals(Instant.MAX, interval.to());
    }

    @Test
    @DisplayName("open() with both null spans all time")
    void openWithBothNullSpansAllTime() {
        Interval interval = Interval.open(null, null);
        assertEquals(Instant.MIN, interval.from());
        assertEquals(Instant.MAX, interval.to());
    }

    @Test
    @DisplayName("contains instant within range")
    void containsInstantWithinRange() {
        Interval interval = new Interval(T2024_01_01, T2024_12_31);
        assertTrue(interval.contains(T2024_06_01));
    }

    @Test
    @DisplayName("contains instant at start boundary (inclusive)")
    void containsAtStartBoundary() {
        Interval interval = new Interval(T2024_01_01, T2024_12_31);
        assertTrue(interval.contains(T2024_01_01));
    }

    @Test
    @DisplayName("contains instant at end boundary (inclusive)")
    void containsAtEndBoundary() {
        Interval interval = new Interval(T2024_01_01, T2024_12_31);
        assertTrue(interval.contains(T2024_12_31));
    }

    @Test
    @DisplayName("does not contain instant before start")
    void doesNotContainBeforeStart() {
        Interval interval = new Interval(T2024_06_01, T2024_12_31);
        assertFalse(interval.contains(T2024_01_01));
    }

    @Test
    @DisplayName("does not contain instant after end")
    void doesNotContainAfterEnd() {
        Interval interval = new Interval(T2024_01_01, T2024_06_01);
        assertFalse(interval.contains(T2024_12_31));
    }

    @Test
    @DisplayName("contains() throws on null")
    void containsThrowsOnNull() {
        Interval interval = new Interval(T2024_01_01, T2024_12_31);
        assertThrows(NullPointerException.class, () -> interval.contains(null));
    }

    @Test
    @DisplayName("overlaps when intervals share a range")
    void overlapsWhenSharedRange() {
        Interval a = new Interval(T2024_01_01, T2024_12_31);
        Interval b = new Interval(T2024_06_01, T2024_12_31);
        assertTrue(a.overlaps(b));
        assertTrue(b.overlaps(a)); // symmetric
    }

    @Test
    @DisplayName("overlaps at a single boundary point")
    void overlapsAtBoundaryPoint() {
        Interval a = new Interval(T2024_01_01, T2024_06_01);
        Interval b = new Interval(T2024_06_01, T2024_12_31);
        assertTrue(a.overlaps(b));
    }

    @Test
    @DisplayName("does not overlap when completely separate")
    void doesNotOverlapWhenSeparate() {
        Interval a = new Interval(T2024_01_01, T2024_06_01);
        Interval b = new Interval(T2024_12_31, Instant.parse("2025-01-01T00:00:00Z"));
        assertFalse(a.overlaps(b));
    }

    @Test
    @DisplayName("overlaps() throws on null")
    void overlapsThrowsOnNull() {
        Interval interval = new Interval(T2024_01_01, T2024_12_31);
        assertThrows(NullPointerException.class, () -> interval.overlaps(null));
    }

    @Test
    @DisplayName("is adjacent when this.to equals other.from")
    void isAdjacentWhenThisToEqualsOtherFrom() {
        Interval a = new Interval(T2024_01_01, T2024_06_01);
        Interval b = new Interval(T2024_06_01, T2024_12_31);
        assertTrue(a.isAdjacentTo(b));
    }

    @Test
    @DisplayName("is adjacent when other.to equals this.from")
    void isAdjacentWhenOtherToEqualsThisFrom() {
        Interval a = new Interval(T2024_06_01, T2024_12_31);
        Interval b = new Interval(T2024_01_01, T2024_06_01);
        assertTrue(a.isAdjacentTo(b));
    }

    @Test
    @DisplayName("not adjacent when gap exists between intervals")
    void notAdjacentWhenGapExists() {
        Interval a = new Interval(T2024_01_01, T2024_06_01);
        Interval b = new Interval(T2024_12_31, Instant.parse("2025-01-01T00:00:00Z"));
        assertFalse(a.isAdjacentTo(b));
    }

    @Test
    @DisplayName("earlier start sorts first")
    void earlierStartSortsFirst() {
        Interval a = new Interval(T2024_01_01, T2024_12_31);
        Interval b = new Interval(T2024_06_01, T2024_12_31);
        assertTrue(a.compareTo(b) < 0);
    }

    @Test
    @DisplayName("same start — shorter interval sorts first")
    void sameStartShorterSortsFirst() {
        Interval a = new Interval(T2024_01_01, T2024_06_01);
        Interval b = new Interval(T2024_01_01, T2024_12_31);
        assertTrue(a.compareTo(b) < 0);
    }

    @Test
    @DisplayName("equal intervals compare to zero")
    void equalIntervalsCompareToZero() {
        Interval a = new Interval(T2024_01_01, T2024_12_31);
        Interval b = new Interval(T2024_01_01, T2024_12_31);
        assertEquals(0, a.compareTo(b));
    }

    @Test
    @DisplayName("equal when from and to are the same")
    void equalWhenSameFromAndTo() {
        Interval a = new Interval(T2024_01_01, T2024_12_31);
        Interval b = new Interval(T2024_01_01, T2024_12_31);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("not equal when from differs")
    void notEqualWhenFromDiffers() {
        assertNotEquals(new Interval(T2024_01_01, T2024_12_31), new Interval(T2024_06_01, T2024_12_31));
    }

    @Test
    @DisplayName("toString contains from and to")
    void toStringContainsFromAndTo() {
        Interval interval = new Interval(T2024_01_01, T2024_12_31);
        String str = interval.toString();
        assertTrue(str.contains(T2024_01_01.toString()));
        assertTrue(str.contains(T2024_12_31.toString()));
    }
}