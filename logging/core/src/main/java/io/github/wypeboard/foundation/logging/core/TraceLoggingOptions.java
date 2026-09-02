package io.github.wypeboard.foundation.logging.core;

import org.slf4j.event.Level;

import java.util.Set;

public record TraceLoggingOptions(
        Level level,
        boolean logArgs,
        boolean logResult,
        boolean logExecutionTime,
        int maxLength,
        Set<String> fieldsToMask
) {

    public static TraceLoggingOptions defaults() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Level level = Level.INFO;
        private boolean logArgs = true;
        private boolean logResult = true;
        private boolean logExecutionTime = true;
        private int maxLength = 4000;
        private Set<String> fields = Set.of();

        private Builder() {
            // Empty constructur
        }

        public Builder level(Level level) {
            this.level = level;
            return this;
        }

        public Builder logArgs(boolean v) {
            this.logArgs = v;
            return this;
        }

        public Builder logResult(boolean v) {
            this.logResult = v;
            return this;
        }

        public Builder logExecutionTime(boolean v) {
            this.logExecutionTime = v;
            return this;
        }

        public Builder maxLength(int v) {
            this.maxLength = v;
            return this;
        }

        public Builder fieldsToMask(Set<String> fields) {
            this.fields = fields;
            return this;
        }

        public TraceLoggingOptions build() {
            return new TraceLoggingOptions(level, logArgs, logResult, logExecutionTime, maxLength, fields);
        }
    }
}
