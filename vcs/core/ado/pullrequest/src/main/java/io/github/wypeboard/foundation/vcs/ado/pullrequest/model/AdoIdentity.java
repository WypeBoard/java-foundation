package io.github.wypeboard.foundation.vcs.ado.pullrequest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.wypeboard.foundation.vcs.ado.pullrequest.model.api.Identifiable;

/**
 * General identity type, used by the undocumented {@link AdoConnectionData} structure.
 */
@JsonIgnoreProperties(ignoreUnknown = true, allowGetters = true)
public class AdoIdentity implements Identifiable {
    private String id;
    private String providerDisplayName;

    public AdoIdentity() {
    }

    @Override
    public String getId() {
        return id;
    }

    public String getProviderDisplayName() {
        return providerDisplayName;
    }
}
