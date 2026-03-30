package io.github.wypeboard.foundation.vcs.base.connector;

import io.github.wypeboard.foundation.vcs.base.connector.api.VcsAuthStrategy;
import io.github.wypeboard.foundation.vcs.base.utils.VcsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;

public class BasicAuthStrategy implements VcsAuthStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicAuthStrategy.class);
    private final String authToken;

    public BasicAuthStrategy(String authToken) {
        if (authToken == null || authToken.isEmpty()) {
            LOGGER.error("No auth token provided - API calls will fail");
        }
        this.authToken = authToken;
    }

    @Override
    public void applyHeaders(HttpURLConnection connection, String contentType) {
        connection.setRequestProperty("Authorization", VcsUtils.formatAuthToken(authToken, "Basic"));
        connection.setRequestProperty("Content-Type", contentType);
        connection.setRequestProperty("Accept", "application/json");
    }
}
