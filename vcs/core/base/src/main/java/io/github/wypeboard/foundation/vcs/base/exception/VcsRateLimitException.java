package io.github.wypeboard.foundation.vcs.base.exception;

/**
 * Thrown when API rate limit is hit
 */
public class VcsRateLimitException extends VcsException {
    private final Integer retryAfterSeconds;

    public VcsRateLimitException(String message, Integer retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public Integer getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}