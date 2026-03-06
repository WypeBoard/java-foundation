package io.github.wypeboard.foundation.date.core.domain;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public class Interval implements Serializable, Comparable<Interval> {

    private final Instant from;
    private final Instant to;

    public Interval(Instant from, Instant to) {
        Objects.requireNonNull(from, "from cannot be null — use Interval.open() for unbounded intervals");
        Objects.requireNonNull(to, "to cannot be null — use Interval.open() for unbounded intervals");
        if (to.isBefore(from)) {
            throw new IllegalArgumentException(
                    "to [%s] cannot be before from [%s]".formatted(to, from));
        }
        this.from = from;
        this.to = to;
    }

    public static Interval open(Instant from, Instant to) {
        if (from == null) {
            from = Instant.MIN;
        }
        if (to == null) {
            to = Instant.MAX;
        }
        return new Interval(from, to);
    }

    public Instant from() {
        return from;
    }

    public Instant to() {
        return to;
    }

    public boolean contains(Instant instant) {
        Objects.requireNonNull(instant, "instant cannot be null");
        return !instant.isBefore(from) && !instant.isAfter(to);
    }

    public boolean overlaps(Interval other) {
        Objects.requireNonNull(other, "other cannot be null");
        return !this.to.isBefore(other.from) && !other.to.isBefore(this.from);
    }

    public boolean isAdjacentTo(Interval other) {
        Objects.requireNonNull(other, "other cannot be null");
        return this.to.equals(other.from) || other.to.equals(this.from);
    }

    @Override
    public int compareTo(Interval other) {
        Objects.requireNonNull(other, "other cannot be null");
        int fromComparison = this.from.compareTo(other.from);
        if (fromComparison != 0) {
            return fromComparison;
        }
        return this.to.compareTo(other.to);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Interval other)) return false;
        return from.equals(other.from) && to.equals(other.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    @Override
    public String toString() {
        return "[%s, %s]".formatted(from, to);
    }
}
