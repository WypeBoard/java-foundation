package io.github.wypeboard.foundation.logging.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.concurrent.Callable;

/**
 * Trace logger.
 * <p>
 * Uses {@link org.slf4j.Logger}.
 *
 * <h2>Explicit usage</h2>
 * <pre>{@code
 * private static final TraceLogger TRACE =
 *         TraceLogger.forClass(OrderService.class);
 *
 * public Order createOrder(CreateOrderRequest req) {
 *     return TRACE.trace("createOrder", TraceLoggingOptions.defaults(),
 *             () -> doCreateOrder(req), req);
 * }
 * }</pre>
 *
 * <h2>Disabling at runtime</h2>
 * Call {@link #setEnabled(boolean)} or configure the underlying logger
 * level to {@code OFF}. Nothing is logged when disabled.
 */
public final class TraceLogger {

    private final Logger logger;
    private volatile boolean enabled = true;

    public TraceLogger(Logger logger) {
        this.logger = logger;
    }

    public static TraceLogger forClass(Class<?> clazz) {
        return new TraceLogger(LoggerFactory.getLogger(clazz.getName()));
    }

    public static TraceLogger forName(String name) {
        return new TraceLogger(LoggerFactory.getLogger(name));
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Wraps {@code callable}, logging entry, exit, timing, and any exception.
     *
     * @param methodName simple method name shown in log messages
     * @param opts       logging options for this invocation
     * @param callable   the work to execute
     * @param args       method arguments (used when {@code opts.logArgs()} is true)
     * @param <T>        return type
     * @return whatever {@code callable} returns
     * @throws Exception whatever {@code callable} throws (always re-thrown)
     */
    public <T> T trace(String methodName, TraceLoggingOptions opts,
                       Callable<T> callable, Object... args) throws Exception {
        if (!enabled || !isLevelEnabled(opts.level())) {
            return callable.call();
        }

        String className = loggerSimpleName();
        log(opts.level(),
                TraceLoggerFormatter.entryMessage(opts),
                TraceLoggerFormatter.entryArgs(className, methodName, args, opts));

        long start = System.currentTimeMillis();
        try {
            T result = callable.call();
            log(opts.level(),
                    TraceLoggerFormatter.exitMessage(opts),
                    TraceLoggerFormatter.exitArgs(className, methodName, result,
                            System.currentTimeMillis() - start, opts));
            return result;
        } catch (Exception ex) {
            logger.atError()
                    .log(TraceLoggerFormatter.exceptionMessage(),
                            TraceLoggerFormatter.exceptionArgs(className, methodName,
                                    System.currentTimeMillis() - start, ex));
            throw ex;
        }
    }

    /**
     * Void variant — wraps a {@link ThrowingRunnable}.
     */
    public void traceVoid(String methodName, TraceLoggingOptions opts,
                          ThrowingRunnable runnable, Object... args) throws Exception {
        trace(methodName, opts, () -> { runnable.run(); return null; }, args);
    }

    // --- Helpers ---

    private boolean isLevelEnabled(Level level) {
        return switch (level) {
            case TRACE -> logger.isTraceEnabled();
            case DEBUG -> logger.isDebugEnabled();
            case WARN  -> logger.isWarnEnabled();
            case ERROR -> logger.isErrorEnabled();
            default    -> logger.isInfoEnabled();
        };
    }

    private void log(Level level, String message, Object... args) {
        switch (level) {
            case TRACE -> logger.atTrace().log(message, args);
            case DEBUG -> logger.atDebug().log(message, args);
            case WARN  -> logger.atWarn().log(message, args);
            case ERROR -> logger.atError().log(message, args);
            default    -> logger.atInfo().log(message, args);
        }
    }

    /**
     * Derives a simple class name from the logger name, e.g. "com.example.Foo" → "Foo".
     */
    private String loggerSimpleName() {
        String name = logger.getName();
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot + 1) : name;
    }

    // --- Supporting type ---

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }
}
