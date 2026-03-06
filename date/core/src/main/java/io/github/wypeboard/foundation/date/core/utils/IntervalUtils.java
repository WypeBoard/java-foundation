package io.github.wypeboard.foundation.date.core.utils;

import io.github.wypeboard.foundation.date.core.domain.Interval;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class IntervalUtils {

    private IntervalUtils() {
        // Utils class
    }

    /**
     * Merges overlapping or adjacent intervals into the smallest set of
     * non-overlapping intervals that covers the same range.
     * <p>
     * [1--3] [2--5] [7--9]  →  [1--5] [7--9]
     */
    public static List<Interval> normalize(Collection<Interval> intervals) {
        if (intervals == null || intervals.isEmpty()) {
            return List.of();
        }

        List<Interval> sorted = new ArrayList<>(intervals);
        Collections.sort(sorted);

        List<Interval> result = new ArrayList<>();
        Interval current = sorted.getFirst();

        for (int i = 1; i < sorted.size(); i++) {
            Interval next = sorted.get(i);
            if (current.overlaps(next) || current.isAdjacentTo(next)) {
                // Merge — extend current to the furthest 'to'
                Instant mergedTo = current.to().isAfter(next.to()) ? current.to() : next.to();
                current = new Interval(current.from(), mergedTo);
            } else {
                result.add(current);
                current = next;
            }
        }
        result.add(current);
        return Collections.unmodifiableList(result);
    }

    /**
     * Splits intervals at every boundary point, producing the smallest
     * atomic (non-overlapping) sub-intervals.
     * <p>
     * [1--5] [3--7]  →  [1--3] [3--5] [5--7]
     */
    public static List<Interval> atomize(Collection<Interval> intervals) {
        if (intervals == null || intervals.isEmpty()) return List.of();

        // Collect all unique boundary points and sort them
        List<Instant> boundaries = intervals.stream()
                .flatMap(i -> java.util.stream.Stream.of(i.from(), i.to()))
                .distinct()
                .sorted()
                .toList();

        // Build sub-intervals between each consecutive pair of boundaries
        List<Interval> atoms = new ArrayList<>();
        for (int i = 0; i < boundaries.size() - 1; i++) {
            atoms.add(new Interval(boundaries.get(i), boundaries.get(i + 1)));
        }
        return Collections.unmodifiableList(atoms);
    }

    /**
     * Returns all intervals from the collection that overlap with the given interval.
     */
    public static List<Interval> overlappingWith(Collection<Interval> intervals, Interval target) {
        Objects.requireNonNull(target, "target cannot be null");
        return intervals.stream()
                .filter(i -> i.overlaps(target))
                .toList();
    }

    /**
     * Returns all intervals that contain the given instant.
     */
    public static List<Interval> containing(Collection<Interval> intervals, Instant instant) {
        Objects.requireNonNull(instant, "instant cannot be null");
        return intervals.stream()
                .filter(i -> i.contains(instant))
                .toList();
    }

    /**
     * Returns the gap between two non-overlapping intervals, or empty if they overlap/touch.
     */
    public static java.util.Optional<Interval> gap(Interval a, Interval b) {
        Interval earlier = a.compareTo(b) <= 0 ? a : b;
        Interval later = a.compareTo(b) <= 0 ? b : a;

        if (earlier.overlaps(later) || earlier.isAdjacentTo(later)) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(new Interval(earlier.to(), later.from()));
    }
}
