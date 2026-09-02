package io.github.wypeboard.foundation.logging.spring.tracelogging;

import io.github.wypeboard.foundation.logging.core.TraceLoggerFormatter;
import io.github.wypeboard.foundation.logging.core.TraceLoggingOptions;
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
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Aspect
@Component
public class TraceLoggingAspect {

    @Around("@annotation(traceLogging) || @within(traceLogging)")
    public Object trace(ProceedingJoinPoint pjp, TraceLogging traceLogging) throws Throwable {
        Logger logger = LoggerFactory.getLogger(pjp.getTarget().getClass());
        Level level = traceLogging.level();

        if (!isLevelEnabled(logger, level)) {
            return pjp.proceed();
        }

        TraceLoggingOptions options = optionsFrom(traceLogging);
        String className = pjp.getTarget().getClass().getSimpleName();
        String methodName = pjp.getSignature().getName();

        log(logger,
                level,
                TraceLoggerFormatter.entryMessage(options),
                TraceLoggerFormatter.entryArgs(className, methodName, pjp.getArgs(), options)
        );

        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            log(logger,
                    level,
                    TraceLoggerFormatter.exitMessage(options),
                    TraceLoggerFormatter.exitArgs(className, methodName, result, System.currentTimeMillis() - start, options)
            );
            return result;
        } catch (Throwable ex) {
            logger
                    .atError()
                    .log(TraceLoggerFormatter.exceptionMessage(),
                            TraceLoggerFormatter.exceptionArgs(className, methodName, System.currentTimeMillis() - start, ex));
            throw ex;
        }
    }

    private TraceLoggingOptions optionsFrom(TraceLogging ann) {
        return TraceLoggingOptions.builder()
                .level(ann.level())
                .logArgs(ann.logArgs())
                .logResult(ann.logResult())
                .logExecutionTime(ann.logExecutionTime())
                .maxLength(ann.maxLength())
                .fieldsToMask(Set.of(ann.fieldsToMask()))
                .build();
    }

    private boolean isLevelEnabled(Logger logger, Level level) {
        return switch (level) {
            case TRACE -> logger.isTraceEnabled();
            case DEBUG -> logger.isDebugEnabled();
            case WARN -> logger.isWarnEnabled();
            case ERROR -> logger.isErrorEnabled();
            default -> logger.isInfoEnabled();
        };
    }

    private void log(Logger logger, Level level, String message, Object... args) {
        switch (level) {
            case TRACE -> logger.atTrace().log(message, args);
            case DEBUG -> logger.atDebug().log(message, args);
            case WARN -> logger.atWarn().log(message, args);
            case ERROR -> logger.atError().log(message, args);
            default -> logger.atInfo().log(message, args);
        }
    }
}
