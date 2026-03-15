package io.github.wypeboard.foundation.vcs.ado.pullrequest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.wypeboard.adoassistant.infrastructure.vcs.connector.ado.model.api.Identifiable;

/**
 * Generic identity, representing e.g. a person or group.
 *
 * @see <a href="https://learn.microsoft.com/en-us/rest/api/azure/devops/core/teams/get-team-members-with-extended-properties?view=azure-devops-rest-7.1&tabs=HTTP#identityref">Definition in Azure DevOps docs</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true, allowGetters = true)
public class AdoIdentityRef implements Identifiable {
    private String id;
    private String descriptor;
    private String displayName;
    @JsonProperty("isDeletedInOrigin")
    private boolean isDeletedInOrigin;
    private String url;
    private String uniqueName;

    public AdoIdentityRef() {
    }

    @Override
    public String getId() {
        return id;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isDeletedInOrigin() {
        return isDeletedInOrigin;
    }

    public String getUrl() {
        return url;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    @JsonIgnore
    public boolean isAuthorASystemUser() {
        return this.uniqueName.isEmpty();
    }

    @JsonIgnore
    public boolean isAuthorAUser() {
        return !isAuthorASystemUser();
    }
}
