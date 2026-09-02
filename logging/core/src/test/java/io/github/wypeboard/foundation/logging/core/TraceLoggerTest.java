package io.github.wypeboard.foundation.logging.core;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TraceLoggerTest {

    private static final String LOGGER_NAME =
            "io.github.wypeboard.foundation.logging.core.TraceLoggerTest";

    private ListAppender<ILoggingEvent> appender;
    private ch.qos.logback.classic.Logger logbackLogger;
    private TraceLogger traceLogger;

    @BeforeEach
    void setUp() {
        logbackLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(LOGGER_NAME);
        logbackLogger.setLevel(ch.qos.logback.classic.Level.ALL);

        appender = new ListAppender<>();
        appender.start();
        logbackLogger.addAppender(appender);

        traceLogger = TraceLogger.forName(LOGGER_NAME);
    }

    @AfterEach
    void tearDown() {
        logbackLogger.detachAppender(appender);
    }

    private List<String> messages() {
        return appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .toList();
    }

    private void assertContains(String fragment) {
        assertTrue(messages().stream().anyMatch(m -> m.contains(fragment)),
                "Expected message containing: " + fragment + "\nActual: " + messages());
    }

    @Test
    void logsEntryAndExit() throws Exception {
        traceLogger.trace("doWork", TraceLoggingOptions.defaults(), () -> "result", "arg1");
        assertContains("Entering");
        assertContains("Exiting");
    }

    @Test
    void logsArgs() throws Exception {
        traceLogger.trace("doWork", TraceLoggingOptions.defaults(), () -> "r", "hello");
        assertContains("[hello]");
    }

    @Test
    void logsReturnValue() throws Exception {
        traceLogger.trace("doWork", TraceLoggingOptions.defaults(), () -> "my-result");
        assertContains("my-result");
    }

    @Test
    void logsExecutionTime() throws Exception {
        traceLogger.trace("doWork", TraceLoggingOptions.defaults(), () -> null);
        assertContains("ms]");
    }

    @Test
    void suppressesArgsAndResult() throws Exception {
        var opts = TraceLoggingOptions.builder()
                .logArgs(false)
                .logResult(false)
                .build();
        traceLogger.trace("doWork", opts, () -> "secret", "secret-arg");
        assertTrue(messages().stream().noneMatch(m -> m.contains("with args")));
        assertTrue(messages().stream().noneMatch(m -> m.contains("with return")));
    }

    @Test
    void masksFields() throws Exception {
        var opts = TraceLoggingOptions.builder()
                .fieldsToMask(Set.of("password"))
                .build();
        traceLogger.trace("doWork", opts, () -> null, "password=supersecret");
        assertContains("***");
        assertTrue(messages().stream().noneMatch(m -> m.contains("supersecret")));
    }

    @Test
    void truncatesLongValues() throws Exception {
        var opts = TraceLoggingOptions.builder().maxLength(10).build();
        traceLogger.trace("doWork", opts, () -> "short", "this is a very long argument value");
        assertContains("[truncated]");
    }

    @Test
    void logsExceptionAtError() throws Exception {
        assertThrows(IllegalStateException.class, () ->
                traceLogger.trace("boom", TraceLoggingOptions.defaults(), () -> {
                    throw new IllegalStateException("oops");
                })
        );
        assertTrue(appender.list.stream()
                .anyMatch(e -> e.getLevel() == ch.qos.logback.classic.Level.ERROR
                        && e.getFormattedMessage().contains("boom")));
    }

    @Test
    void doesNothingWhenDisabled() throws Exception {
        traceLogger.setEnabled(false);
        traceLogger.trace("doWork", TraceLoggingOptions.defaults(), () -> "result", "arg");
        assertTrue(messages().isEmpty(), "Expected no log output when disabled");
    }

    @Test
    void respectsLogLevel() throws Exception {
        logbackLogger.setLevel(ch.qos.logback.classic.Level.INFO);
        var opts = TraceLoggingOptions.builder().level(Level.DEBUG).build();
        traceLogger.trace("doWork", opts, () -> "result", "arg");
        assertTrue(messages().isEmpty(), "Expected no output at INFO when opts level is DEBUG");
    }
}