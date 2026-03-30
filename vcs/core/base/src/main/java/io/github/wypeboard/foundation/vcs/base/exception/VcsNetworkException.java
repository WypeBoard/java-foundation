package io.github.wypeboard.foundation.vcs.base.exception;

/**
 * Thrown when network operations timeout or fail
 */
public class VcsNetworkException extends VcsException {
    public VcsNetworkException(String message) {
        super(message);
    }

    public VcsNetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
