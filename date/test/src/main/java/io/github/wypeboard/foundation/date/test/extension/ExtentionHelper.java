package io.github.wypeboard.foundation.date.test.extension;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstances;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

public final class ExtentionHelper {

    private ExtentionHelper() {
        // Utility class
    }


    public static <T extends Annotation> Optional<T> retrieveAnnotationFromTestClasses(Class<T> annotationClass, ExtensionContext context) {
        ExtensionContext currentContext = context;
        Optional<T> annotation;

        do {
            annotation = findAnnotation(currentContext.getElement(), annotationClass);

            if (currentContext.getParent().isEmpty()) {
                break;
            }

            currentContext = currentContext.getParent().get();
        } while (annotation.isEmpty() && currentContext != context.getRoot());

        return annotation;
    }

    public static <A> Set<A> getFieldsFromInstanceHierarchy(ExtensionContext context, Class<A> fieldType) {
        Set<A> fields = new HashSet<>();
        ExtensionContext current = context;

        while (current != null) {
            current.getTestInstances().map(TestInstances::getAllInstances)
                    .ifPresent(testinstances -> {
                        testinstances.forEach(testInstance -> {
                            Set<A> fieldInInstance = getFieldsOfType(testInstance, fieldType);
                            fields.addAll(fieldInInstance);
                        });
                    });
            current = current.getParent().orElse(null);
        }
        return fields;
    }

    private static <A> Set<A> getFieldsOfType(Object testInstance, Class<A> mockClass) {
        return Arrays.stream(testInstance.getClass().getDeclaredFields())
                .filter(field -> mockClass.isAssignableFrom(field.getType()))
                .map(field -> getField(field, testInstance, mockClass))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private static <A> A getField(Field field, Object testInstance, Class<A> mockClass) {
        field.setAccessible(true);

        try {
            return mockClass.cast(field.get(testInstance));
        } catch (IllegalAccessException e) {
            return null;
        }
    }
}
