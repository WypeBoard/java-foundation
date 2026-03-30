package io.github.wypeboard.foundation.vcs.base.model.requests;

import com.fasterxml.jackson.annotation.JsonValue;

public enum JsonPatchOperation {
    ADD("add"),
    COPY("copy"),
    MOVE("move"),
    REMOVE("remove"),
    REPLACE("replace"),
    TEST("test"),
    ;
    private final String value;

    JsonPatchOperation(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
