package io.github.wypeboard.foundation.logging.spring.tracelogging;


import org.slf4j.event.Level;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TraceLogging {
    //@AliasFor("level")
    //Level value() default Level.INFO;

    //@AliasFor("value")
    Level level() default Level.INFO;

    boolean logArgs() default true;
    boolean logResult() default true;
    boolean logExecutionTime() default true;
    int maxLength() default 4000;
    String[] fieldsToMask() default {};

}
