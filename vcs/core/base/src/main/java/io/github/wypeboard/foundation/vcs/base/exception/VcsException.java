package io.github.wypeboard.foundation.vcs.base.exception;

/**
 * Base exception for all ADO-related errors
 */
public class VcsException extends RuntimeException {

    public VcsException(String message) {
        super(message);
    }

    public VcsException(String message, Throwable cause) {
        super(message, cause);
    }
}
