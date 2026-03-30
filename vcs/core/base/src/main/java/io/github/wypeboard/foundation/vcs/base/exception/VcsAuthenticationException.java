package io.github.wypeboard.foundation.vcs.base.exception;

/**
 * Thrown when authentication fails (invalid PAT token)
 */
public class VcsAuthenticationException extends VcsException {

    public VcsAuthenticationException(String message) {
        super(message);
    }

    public VcsAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
