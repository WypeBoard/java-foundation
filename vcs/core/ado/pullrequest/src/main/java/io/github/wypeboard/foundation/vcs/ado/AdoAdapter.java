package io.github.wypeboard.foundation.vcs.ado;

import io.github.wypeboard.foundation.vcs.ado.pullrequest.model.AdoGitPullRequest;
import io.github.wypeboard.foundation.vcs.ado.pullrequest.model.AdoThread;
import io.github.wypeboard.foundation.vcs.base.exception.VcsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AdoAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdoAdapter.class);
    private final AdoConnectorClient adoConnectorClient;

    public AdoAdapter(ConfigManager configManager, PropertiesLoader propertiesLoader) {
        String repository = propertiesLoader.get(PropertiesConstants.ADO_REPOSITORY);
        String authToken = propertiesLoader.get(PropertiesConstants.ADO_AUTH_TOKEN);
        adoConnectorClient = new AdoConnectorClient(repository, authToken, configManager);
    }

    public void fetchGenericPullRequestData(DataAggregator dataAggregator) {
        LOGGER.info("Fetching data from ADO");
        long startTime = System.currentTimeMillis();

        try {
            List<AdoGitPullRequest> pullRequests = this.adoConnectorClient.getPullRequests();
            long duration = System.currentTimeMillis() - startTime;

            LOGGER.info("Succesfully fetched {} pull requests in {}ms", pullRequests.size(), duration);
            dataAggregator.withPullRequests(this.adoConnectorClient.getPullRequests());
        } catch (VcsException e) {
            long duration = System.currentTimeMillis() - startTime;
            LOGGER.error("Failed to fetch pull requests after {}ms: {}", duration, e.getMessage(), e);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LOGGER.error("Unexpected error fetching pull requests after {}ms: {}", duration, e.getMessage(), e);
            throw new VcsException("Unexpected error during pull request fetching", e);
        }
    }

    public void fetchPullrequestThreads(DataAggregator dataAggregator) {
        int totalPullRequests = dataAggregator.getPullRequests().size();
        LOGGER.info("Starting thread data fetch for {} PRs", totalPullRequests);
        long startTime = System.currentTimeMillis();

        int successCount = 0;
        int failCount = 0;
        try {
            for (Integer pullRequestId : dataAggregator.getPullRequestIds()) {
                try {
                    LOGGER.debug("Fetching threads for PR #{}", pullRequestId);
                    List<AdoThread> commentThreads = this.adoConnectorClient.getCommentThreads(pullRequestId);
                    dataAggregator.withThreads(pullRequestId, commentThreads);
                    successCount++;

                    LOGGER.debug("PR #{}: {} threads found", pullRequestId, commentThreads.size());
                } catch (VcsException e) {
                    failCount++;
                    LOGGER.error("Failed to fetch threads for PR#{}", pullRequestId, e);
                    // Continue with other Prs - Don't fail the operation!
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            LOGGER.info("Thread fetch complete: {} succeeded, {} failed in {}ms ({}% success rate)",
                    successCount, failCount, duration,
                    totalPullRequests > 0 ? (successCount * 100 / totalPullRequests) : 0);

            if (successCount == 0 && totalPullRequests > 0) {
                throw new VcsException("Failed to fetch threads for any pull requests");
            }

        } catch (VcsException e) {
            throw e;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LOGGER.error("Unexpected error during thread fetching after {}ms", duration, e);
            throw new VcsException("Unexpected error during thread fetching", e);
        }
    }

}
