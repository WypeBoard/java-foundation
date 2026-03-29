package io.github.wypeboard.foundation.logging.spring.tracelogging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.github.wypeboard.foundation.logging.spring.tracelogging.fixture.TraceLoggingTestService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TraceLoggingAspectTest {

    private final TraceLoggingAspect aspect = new TraceLoggingAspect();

    private ListAppender<ILoggingEvent> logAppender;
    private ch.qos.logback.classic.Logger aspectLogger;

    @BeforeEach
    void attachAppender() {
        aspectLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(TraceLoggingTestService.class);

        logAppender = new ListAppender<>();
        logAppender.start();
        aspectLogger.addAppender(logAppender);
    }

    @AfterEach
    void detachAppender() {
        aspectLogger.detachAppender(logAppender);
    }

    private ProceedingJoinPoint mockJoinPoint(Object target, String methodName, Object... args) throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);

        when(pjp.getTarget()).thenReturn(target);
        when(pjp.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn(methodName);
        when(pjp.getArgs()).thenReturn(args);
        when(pjp.proceed()).thenReturn("mocked-result");

        return pjp;
    }
    // --- Helpers ---

    private List<String> logMessages() {
        return logAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .toList();
    }

    private void assertHasMessageContaining(String fragment) {
        assertTrue(
                logMessages().stream().anyMatch(m -> m.contains(fragment)),
                "Expected a log message containing: " + fragment + "\nActual: " + logMessages()
        );
    }

    // --- Tests ---

    @Test
    void shouldLogEntryAndExit() throws Throwable {
        var pjp = mockJoinPoint(new TraceLoggingTestService(), "greet", "World");

        aspect.trace(pjp, annotation("defaults"));

        assertHasMessageContaining("Entering TraceLoggingTestService.greet");
        assertHasMessageContaining("Exiting TraceLoggingTestService.greet");
    }

    @Test
    void shouldLogArgs() throws Throwable {
        var pjp = mockJoinPoint(new TraceLoggingTestService(), "greet", "World");

        aspect.trace(pjp, annotation("defaults"));

        assertHasMessageContaining("[World]");
    }

    @Test
    void shouldLogReturnValue() throws Throwable {
        var pjp = mockJoinPoint(new TraceLoggingTestService(), "greet", "World");

        aspect.trace(pjp, annotation("defaults"));

        assertHasMessageContaining("mocked-result");
    }

    @Test
    void shouldLogExecutionTime() throws Throwable {
        var pjp = mockJoinPoint(new TraceLoggingTestService(), "greet", "World");

        aspect.trace(pjp, annotation("defaults"));

        assertHasMessageContaining("ms]");
    }

    @Test
    void shouldSkipArgsAndResultWhenDisabled() throws Throwable {
        var pjp = mockJoinPoint(new TraceLoggingTestService(), "noArgsNoResult");

        aspect.trace(pjp, annotation("noArgsNoResult"));

        assertTrue(logMessages().stream().noneMatch(m -> m.contains("with args")));
        assertTrue(logMessages().stream().noneMatch(m -> m.contains("with return")));
    }

    @Test
    void shouldLogExceptionWithErrorLevel() throws Throwable {
        var pjp = mockJoinPoint(new TraceLoggingTestService(), "throwingMethod");
        when(pjp.proceed()).thenThrow(new IllegalArgumentException("boom"));

        assertThrows(IllegalArgumentException.class,
                () -> aspect.trace(pjp, annotation("defaults")));

        assertTrue(
                logAppender.list.stream()
                        .anyMatch(e -> e.getLevel() == ch.qos.logback.classic.Level.ERROR
                                && e.getFormattedMessage().contains("throwingMethod")),
                "Expected an ERROR log for the thrown exception"
        );
    }

    @Test
    void shouldMaskSensitiveFields() throws Throwable {
        var pjp = mockJoinPoint(new TraceLoggingTestService(), "withSensitiveArg", "password=supersecret");

        aspect.trace(pjp, annotation("withMask"));

        assertHasMessageContaining("***");
        assertTrue(logMessages().stream().noneMatch(m -> m.contains("supersecret")));
    }

    @Test
    void shouldTruncateLongOutput() throws Throwable {
        var pjp = mockJoinPoint(new TraceLoggingTestService(), "withLongResult");
        when(pjp.proceed()).thenReturn("this is a very long result that should be truncated");

        aspect.trace(pjp, annotation("withMaxLength"));

        assertHasMessageContaining("[truncated]");
    }

    @Test
    void shouldRespectLogLevel() throws Throwable {
        aspectLogger.setLevel(ch.qos.logback.classic.Level.INFO);
        var pjp = mockJoinPoint(new TraceLoggingTestService(), "debugLevel", "hello");

        aspect.trace(pjp, annotation("debugLevel"));

        assertTrue(logMessages().isEmpty(), "Expected no log output at INFO when annotation is DEBUG");
    }

    private static class AnnotationHolder {
        @TraceLogging
        void defaults() {}

        @TraceLogging(logArgs = false, logResult = false)
        void noArgsNoResult() {}

        @TraceLogging(level = Level.DEBUG)
        void debugLevel() {}

        @TraceLogging(fieldsToMask = {"password"})
        void withMask() {}

        @TraceLogging(maxLength = 10)
        void withMaxLength() {}
    }

    private TraceLogging annotation(String methodName) throws NoSuchMethodException {
        return AnnotationHolder.class
                .getDeclaredMethod(methodName)
                .getAnnotation(TraceLogging.class);
    }
}