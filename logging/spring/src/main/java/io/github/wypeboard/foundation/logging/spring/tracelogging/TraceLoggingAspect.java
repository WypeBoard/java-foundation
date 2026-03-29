package io.github.wypeboard.foundation.logging.spring.tracelogging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Aspect
@Component
public class TraceLoggingAspect {

    @Around("@annotation(traceLogging) || @within(traceLogging)")
    public Object trace(ProceedingJoinPoint pjp, TraceLogging traceLogging) throws Throwable {
        Logger logger = LoggerFactory.getLogger(pjp.getTarget().getClass());

        logMethodEntry(logger, pjp, traceLogging);

        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            logMethodExit(logger, pjp, traceLogging, result, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable ex) {
            logMethodException(logger, pjp, traceLogging, ex, System.currentTimeMillis() - start);
            throw ex;
        }
    }

    private void logMethodEntry(Logger logger, ProceedingJoinPoint pjp, TraceLogging ann) {
        if (!isEnabled(logger, ann.level())) return;

        String message = ann.logArgs()
                ? "Entering {}.{} with args {}"
                : "Entering {}.{}";

        if (ann.logArgs()) {
            log(logger, ann.level(), message,
                    className(pjp),
                    methodName(pjp),
                    formatArgs(pjp.getArgs(), ann));
        } else {
            log(logger, ann.level(), message, className(pjp), methodName(pjp));
        }
    }

    private void logMethodExit(Logger logger, ProceedingJoinPoint pjp, TraceLogging ann,
                               Object result, long elapsedMs) {
        if (!isEnabled(logger, ann.level())) return;

        StringBuilder msg = new StringBuilder("Exiting {}.{}");
        List<Object> args = new ArrayList<>();
        args.add(className(pjp));
        args.add(methodName(pjp));

        if (ann.logExecutionTime()) {
            msg.append(" [{}ms]");
            args.add(elapsedMs);
        }
        if (ann.logResult()) {
            msg.append(" with return {}");
            args.add(formatResult(result, ann));
        }

        log(logger, ann.level(), msg.toString(), args.toArray());
    }

    // --- Exception ---

    private void logMethodException(Logger logger, ProceedingJoinPoint pjp, TraceLogging ann,
                                    Throwable ex, long elapsedMs) {
        // Exceptions always log at ERROR regardless of configured level
        logger.error("Exception in {}.{} after {}ms — threw {}: {}",
                className(pjp),
                methodName(pjp),
                elapsedMs,
                ex.getClass().getSimpleName(),
                ex.getMessage());
    }

    // --- Formatting ---

    private String formatArgs(Object[] args, TraceLogging ann) {
        if (args == null || args.length == 0) return "[]";
        String formatted = Arrays.stream(args)
                .map(arg -> formatValue(arg, ann))
                .collect(Collectors.joining(", ", "[", "]"));
        return truncate(formatted, ann.maxLength());
    }

    private String formatResult(Object result, TraceLogging ann) {
        if (result == null) return "null";
        return truncate(formatValue(result, ann), ann.maxLength());
    }

    private String formatValue(Object value, TraceLogging ann) {
        if (value == null) return "null";
        String str = value.toString();
        // Mask fields by looking for key=value or "key":"value" patterns
        for (String field : ann.fieldsToMask()) {
            str = str.replaceAll(
                    "(?i)(" + Pattern.quote(field) + "\\s*[:=]\\s*)[^,}\\]\"]+",
                    "$1***"
            );
        }
        return str;
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return "null";
        return value.length() > maxLength
                ? value.substring(0, maxLength) + "...[truncated]"
                : value;
    }

    // --- Helpers ---

    private String className(ProceedingJoinPoint pjp) {
        return pjp.getTarget().getClass().getSimpleName();
    }

    private String methodName(ProceedingJoinPoint pjp) {
        return pjp.getSignature().getName();
    }

    private boolean isEnabled(Logger logger, Level level) {
        return switch (level) {
            case TRACE -> logger.isTraceEnabled();
            case DEBUG -> logger.isDebugEnabled();
            case WARN  -> logger.isWarnEnabled();
            case ERROR -> logger.isErrorEnabled();
            default    -> logger.isInfoEnabled();
        };
    }

    private void log(Logger logger, Level level, String message, Object... args) {
        switch (level) {
            case TRACE -> logger.atTrace().log(message, args);
            case DEBUG -> logger.atDebug().log(message, args);
            case WARN  -> logger.atWarn().log(message, args);
            case ERROR -> logger.atError().log(message, args);
            default    -> logger.atInfo().log(message, args);
        }
    }
}
