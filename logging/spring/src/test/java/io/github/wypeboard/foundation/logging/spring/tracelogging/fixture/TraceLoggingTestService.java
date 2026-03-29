package io.github.wypeboard.foundation.logging.spring.tracelogging.fixture;

import io.github.wypeboard.foundation.logging.spring.tracelogging.TraceLogging;
import org.slf4j.event.Level;
import org.springframework.stereotype.Component;

@Component
public class TraceLoggingTestService {

    @TraceLogging
    public String greet(String name) {
        return "Hello, " + name;
    }

    @TraceLogging(logArgs = false, logResult = false)
    public void noArgsNoResult() { }

    @TraceLogging(level = Level.DEBUG)
    public String debugLevel(String input) {
        return input;
    }

    @TraceLogging
    public String throwingMethod() {
        throw new IllegalArgumentException("boom");
    }

    @TraceLogging(fieldsToMask = {"password"})
    public String withSensitiveArg(String password) {
        return "done";
    }

    @TraceLogging(maxLength = 10)
    public String withLongResult() {
        return "this is a very long result that should be truncated";
    }
}