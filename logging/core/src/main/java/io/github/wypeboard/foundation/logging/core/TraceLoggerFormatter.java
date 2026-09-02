package io.github.wypeboard.foundation.logging.core;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class TraceLoggerFormatter {

    private TraceLoggerFormatter() {
        // Utility
    }

    public static String entryMessage(TraceLoggingOptions opts) {
        return opts.logArgs() ? "Entering {}.{} with args {}" : "Entering {}.{}";
    }

    /**
     * Arguments array to pass alongside {@link #entryMessage}.
     * Matches the {@code {}} placeholders in that template exactly.
     */
    public static Object[] entryArgs(String className, String methodName,
                                     Object[] methodArgs, TraceLoggingOptions opts) {
        if (opts.logArgs()) {
            return new Object[]{className, methodName, formatArgs(methodArgs, opts)};
        }
        return new Object[]{className, methodName};
    }

    public static String exitMessage(TraceLoggingOptions opts) {
        StringBuilder msg = new StringBuilder("Exiting {}.{}");
        if (opts.logExecutionTime()) {
            msg.append(" [{}ms]");
        }
        if (opts.logResult()) {
            msg.append(" with return {}");
        }
        return msg.toString();
    }

    /**
     * Arguments array to pass alongside {@link #exitMessage}.
     */
    public static Object[] exitArgs(String className, String methodName,
                                    Object result, long elapsedMs,
                                    TraceLoggingOptions opts) {
        if (opts.logExecutionTime() && opts.logResult()) {
            return new Object[]{className, methodName, elapsedMs, formatResult(result, opts)};
        }
        if (opts.logExecutionTime()) {
            return new Object[]{className, methodName, elapsedMs};
        }
        if (opts.logResult()) {
            return new Object[]{className, methodName, formatResult(result, opts)};
        }
        return new Object[]{className, methodName};
    }

    public static String exceptionMessage() {
        return "Exception in {}.{} after {}ms — threw {}: {}";
    }

    public static Object[] exceptionArgs(String className, String methodName,
                                         long elapsedMs, Throwable ex) {
        return new Object[]{className, methodName, elapsedMs,
                ex.getClass().getSimpleName(), ex.getMessage()};
    }

    public static String formatArgs(Object[] args, TraceLoggingOptions opts) {
        if (args == null || args.length == 0) return "[]";
        String formatted = Arrays.stream(args)
                .map(a -> formatValue(a, opts))
                .collect(Collectors.joining(", ", "[", "]"));
        return truncate(formatted, opts.maxLength());
    }

    public static String formatResult(Object result, TraceLoggingOptions opts) {
        if (result == null) return "null";
        return truncate(formatValue(result, opts), opts.maxLength());
    }

    public static String formatValue(Object value, TraceLoggingOptions opts) {
        if (value == null) return "null";
        String str = value.toString();
        for (String field : opts.fieldsToMask()) {
            str = str.replaceAll(
                    "(?i)(" + Pattern.quote(field) + "\\s*[:=]\\s*)[^,}\\]\"]+",
                    "$1***"
            );
        }
        return str;
    }

    public static String truncate(String value, int maxLength) {
        if (value == null) return "null";
        return value.length() > maxLength
                ? value.substring(0, maxLength) + "...[truncated]"
                : value;
    }
}
