package io.github.wypeboard.foundation.vcs.base.connector;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.wypeboard.foundation.vcs.base.connector.api.VcsAuthStrategy;
import io.github.wypeboard.foundation.vcs.base.exception.VcsAuthenticationException;
import io.github.wypeboard.foundation.vcs.base.exception.VcsNetworkException;
import io.github.wypeboard.foundation.vcs.base.exception.VcsRateLimitException;
import io.github.wypeboard.foundation.vcs.base.exception.VcsServiceException;
import io.github.wypeboard.foundation.vcs.base.utils.SerializationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class HttpVcsConnector {
    private static final Set<Integer> OK_HTTP_CODES = Set.of(
            HttpURLConnection.HTTP_OK,
            HttpURLConnection.HTTP_CREATED,
            HttpURLConnection.HTTP_ACCEPTED,
            HttpURLConnection.HTTP_NOT_AUTHORITATIVE,
            HttpURLConnection.HTTP_NO_CONTENT,
            HttpURLConnection.HTTP_RESET,
            HttpURLConnection.HTTP_PARTIAL
    );

    private static final int DEFAULT_TIMEOUT_MS = 30_000; // 30 Seconds
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1_000;
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpVcsConnector.class);

    private final VcsAuthStrategy authStrategy;
    private final boolean logRequests;
    private final int timeoutMS;
    private final int maxRetries;

    public HttpVcsConnector(VcsAuthStrategy authStrategy) {
        this(authStrategy, true, DEFAULT_TIMEOUT_MS, DEFAULT_MAX_RETRIES);
    }

    public HttpVcsConnector(VcsAuthStrategy authStrategy, boolean logRequests, int timeoutMS, int maxRetries) {
        this.authStrategy = authStrategy;
        this.logRequests = logRequests;
        this.timeoutMS = timeoutMS;
        this.maxRetries = maxRetries;
    }

    private HttpURLConnection getHttpURLConnection(String requestMethod, String urlString, String contentType) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection uc = (HttpURLConnection) url.openConnection();

        // Set connection properties
        uc.setConnectTimeout(timeoutMS);
        uc.setReadTimeout(timeoutMS);

        // PATCH requires special handling: https://bugs.openjdk.org/browse/JDK-7016595
        if ("PATCH".equals(requestMethod)) {
            uc.setRequestMethod("POST");
            uc.setRequestProperty("X-HTTP-Method-Override", "PATCH");
        } else {
            uc.setRequestMethod(requestMethod);
        }
        uc.setDoOutput(true);
        authStrategy.applyHeaders(uc, contentType);

        return uc;
    }

    /**
     * Send a request and receive the response as raw bytes.
     * @param requestMethod HTTP method
     * @param requestUrl URL to call
     * @return response bytes
     */
    public byte[] sendRequest(String requestMethod, String requestUrl) {
        return sendRequest(requestMethod, requestUrl, null);
    }

    /**
     * Send a request and receive the response as raw bytes.
     * @param requestMethod HTTP method
     * @param requestUrl URL to call
     * @param jsonBody request body
     * @return response bytes
     */
    public byte[] sendRequest(String requestMethod, String requestUrl, String jsonBody) {
        return sendRequest(requestMethod, requestUrl, jsonBody, "application/json; utf-8");
    }

    public byte[] sendRequest(String requestMethod, String requestUrl, String jsonBody, String contentType) {
        return sendRequest(requestMethod, requestUrl, jsonBody, "application/json; utf-8", 0);
    }


    private byte[] sendRequest(String requestMethod, String requestUrl, String jsonBody, String contentType, int attempt) {
        return sendRequestWithRetry(requestMethod, requestUrl, jsonBody, contentType, 0);
    }

    private byte[] sendRequestWithRetry(String requestMethod, String requestUrl, String jsonBody, String contentType, int attempt) {
        if (logRequests) {
            LOGGER.info("Request [attempt {}]: {} {}", attempt + 1, requestMethod, requestUrl);
            if (jsonBody != null) {
                LOGGER.debug("Request body: {}", jsonBody);
            }
        }

        try {
            return executeReqeust(requestMethod, requestUrl, jsonBody, contentType);
        } catch (SocketTimeoutException e) {
            LOGGER.warn("Request timeout on attempt {} for {}", attempt + 1, requestUrl);
            return handleRetry(requestMethod, requestUrl, jsonBody, contentType, attempt, e);
        } catch (VcsServiceException e) {
            // Retry on 5xx errors (server issues) and 429 (rate limit)
            if (e.getStatusCode() >= 500 || e.getStatusCode() == 429) {
                LOGGER.warn("Transient error {} on attempt {} for {}", e.getStatusCode(), attempt + 1, requestUrl);
                return handleRetry(requestMethod, requestUrl, jsonBody, contentType, attempt, e);
            }
            throw e; // Don't retry 4xx errors (except 429)
        } catch (IOException e) {
            LOGGER.warn("Network error on attempt {} for {}: {}", attempt + 1, requestUrl, e.getMessage());
            return handleRetry(requestMethod, requestUrl, jsonBody, contentType, attempt, e);
        }
    }

    private byte[] handleRetry(String requestMethod, String requestUrl, String jsonBody, String contentType, int attempt, Exception originalException) {
        if (attempt >= maxRetries - 1) {
            LOGGER.error("Max retries ({}) exceeded for {}", maxRetries, requestUrl);
            throw new VcsNetworkException("Request failed after " + maxRetries + " attempts: " + requestUrl, originalException);
        }

        try {
            long delay = (long) RETRY_DELAY_MS * (attempt + 1);
            LOGGER.info("Retrying in {}ms...", delay);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new VcsNetworkException("Retry interrupted", e);
        }
        return sendRequestWithRetry(requestMethod, requestUrl, jsonBody, contentType, attempt + 1);
    }

    private byte[] executeReqeust(String requestMethod, String requestUrl, String jsonBody, String contentType) throws IOException {
        final HttpURLConnection uc = getHttpURLConnection(requestMethod, requestUrl, contentType);

        try {
            if (jsonBody != null) {
                try (OutputStream writer = uc.getOutputStream()) {
                    writer.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                    writer.flush();
                }
            }

            int responseCode = uc.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED || responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                String errorBody = readErrorStream(uc);
                LOGGER.error("Authentication failed (HTTP {}): {}", responseCode, errorBody);
                throw new VcsAuthenticationException(
                        "Invalid or expired PAT token. Please check your authentication credentials.",
                        null
                );
            }

            // Check for rate limiting
            if (responseCode == 429) {
                String retryAfter = uc.getHeaderField("Retry-After");
                Integer retrySeconds = retryAfter != null ? Integer.parseInt(retryAfter) : null;
                LOGGER.warn("Rate limit hit. Retry after: {} seconds", retrySeconds);
                throw new VcsRateLimitException("API rate limit exceeded", retrySeconds);
            }

            // Check for server errors
            if (!OK_HTTP_CODES.contains(responseCode)) {
                String errorBody = readErrorStream(uc);
                LOGGER.error("HTTP {} {} for {}: {}", responseCode, uc.getResponseMessage(), requestUrl, errorBody);
                throw new VcsServiceException(responseCode, uc.getResponseMessage(), errorBody);
            }

            byte[] response = uc.getInputStream().readAllBytes();
            LOGGER.debug("Request successful, received {} bytes", response.length);
            return response;
        } finally {
            uc.disconnect();
        }
    }

    private String readErrorStream(HttpURLConnection uc) {
        try {
            if (uc.getErrorStream() != null) {
                return new String(uc.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            LOGGER.warn("Could not read error stream", e);
        }
        return "";
    }

    /**
     * Send a request and parse the received response as a given class.
     * @param requestMethod HTTP method
     * @param requestUrl URL to call
     * @param responseType class to parse the response into
     * @return parsed response
     * @param <T> class to deserialize response into
     */
    public <T> T sendRequestAndParseResponse(String requestMethod, String requestUrl, Class<T> responseType) {
        return sendRequestAndParseResponse(requestMethod, requestUrl, responseType, null);
    }

    /**
     * Send a request and parse the received response as a given class.
     * @param requestMethod HTTP method
     * @param requestUrl URL to call
     * @param responseType class to parse the response into
     * @param jsonBody request body
     * @return parsed response
     * @param <T> class to deserialize response into
     */
    public <T> T sendRequestAndParseResponse(String requestMethod, String requestUrl, Class<T> responseType, String jsonBody) {
        try {
            byte[] bytes = sendRequest(requestMethod, requestUrl, jsonBody);
            return SerializationHelper.getObjectMapper().readValue(bytes, responseType);
        } catch (IOException e) {
            LOGGER.error("Unexpected IOException: ", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Send a request and parse the received response as a given type.
     * @param requestMethod HTTP method
     * @param requestUrl URL to call
     * @param responseType type reference to parse the response as
     * @return parsed response
     * @param <T> class to deserialize response into
     */
    public <T> T sendRequestAndParseResponse(String requestMethod, String requestUrl, TypeReference<T> responseType) {
        return sendRequestAndParseResponse(requestMethod, requestUrl, responseType, null);
    }

    /**
     * Send a request and parse the received response as a given type.
     * @param requestMethod HTTP method
     * @param requestUrl URL to call
     * @param responseType type reference to parse the response as
     * @param jsonBody request body
     * @return parsed response
     * @param <T> class to deserialize response into
     */
    public <T> T sendRequestAndParseResponse(String requestMethod, String requestUrl, TypeReference<T> responseType, String jsonBody) {
        return sendRequestAndParseResponse(requestMethod, requestUrl, responseType, jsonBody, "application/json; utf-8");
    }

    /**
     * Send a JSON Patch request and parse the received response as a given type.
     * @param requestUrl URL to call
     * @param responseType type reference to parse the response as
     * @param jsonBody request body
     * @return parsed response
     * @param <T> class to deserialize response into
     * @see io.github.wypeboard.foundation.vcs.base.model.requests.JsonPatch
     * @see <a href="https://jsonpatch.com/">jsonpatch.com</a>
     */
    public <T> T sendJsonPatchRequestAndParseResponse(String requestUrl, TypeReference<T> responseType, String jsonBody) {
        return sendRequestAndParseResponse("PATCH", requestUrl, responseType, jsonBody, "application/json-patch+json; utf-8");
    }

    private <T> T sendRequestAndParseResponse(String requestMethod, String requestUrl, TypeReference<T> responseType, String jsonBody, String contentType) {
        try {
            byte[] bytes = sendRequest(requestMethod, requestUrl, jsonBody, contentType);
            return SerializationHelper.getObjectMapper().readValue(bytes, responseType);
        } catch (IOException e) {
            LOGGER.error("Unexpected IOException: ", e);
            throw new RuntimeException(e);
        }
    }

}
