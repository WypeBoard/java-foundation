package io.github.wypeboard.foundation.date.test.utils.state;

import java.util.function.Function;
import java.util.function.Predicate;

public final class PredicateUtils {

    public static <T, R> Predicate<T> by(Predicate<R> predicate, Function<T, R> extractor) {
        return value -> predicate.test(extractor.apply(value));
    }

    public static <T> Predicate<T> throwIfNot(Predicate<T> predicate, String errorMessage) {
        return value -> {
            if (!predicate.test(value)) {
                throw new RuntimeException(errorMessage);
            }
            return true;
        };
    }
}
