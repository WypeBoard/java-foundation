package io.github.wypeboard.foundation.vcs.base.exception;

/**
 * Thrown when ADO service returns an error (500, 503, etc.)
 */
public class VcsServiceException extends VcsException {
    private final int statusCode;
    private final String responseBody;

    public VcsServiceException(int statusCode, String message, String responseBody) {
        super(String.format("ADO Service Error [%d]: %s", statusCode, message));
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}