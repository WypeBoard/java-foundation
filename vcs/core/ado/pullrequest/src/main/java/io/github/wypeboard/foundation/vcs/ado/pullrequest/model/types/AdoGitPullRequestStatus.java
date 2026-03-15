package io.github.wypeboard.foundation.vcs.ado.pullrequest.model.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AdoGitPullRequestStatus {
    NOT_SET("notSet"),
    ACTIVE("active"),
    ABANDONED("abandoned"),
    COMPLETED("completed"),
    ALL("all");

    private final String value;

    AdoGitPullRequestStatus(String value) {
        this.value = value;
    }

    @JsonIgnore
    public boolean isAbandoned() {
        return ABANDONED == this;
    }

    @JsonIgnore
    public boolean isCompleted() {
        return COMPLETED == this;
    }

    @JsonIgnore
    public boolean isActive() {
        return ACTIVE == this;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
