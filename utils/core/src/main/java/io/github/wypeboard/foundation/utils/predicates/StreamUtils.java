package io.github.wypeboard.foundation.utils.predicates;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public final class StreamUtils {

    public static <T> Stream<T> ofNullable(Collection<T> collection) {
        return Optional.ofNullable(collection).stream()
                .flatMap(Collection::stream);
    }
}
