package io.github.wypeboard.foundation.vcs.base.connector.api;

import java.net.HttpURLConnection;

public interface VcsAuthStrategy {
    /**
     * Define the Authentication strategy.
     * Each different Version Control System applies authentication differently
     * This interface is to inform the HttpVcsConnector how to apply authentication
     *
     * @param connection the http connection to the Version Control System
     * @param contentType defines how the send content should be interpreted (default json)
     */
    void applyHeaders(HttpURLConnection connection, String contentType);
}
