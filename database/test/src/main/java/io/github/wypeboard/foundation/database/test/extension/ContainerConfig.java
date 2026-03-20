package io.github.wypeboard.foundation.database.test.extension;

import java.util.Arrays;
import java.util.Objects;

public record ContainerConfig(
        String version,
        String database,
        String username,
        String password,
        String[] initScripts
) {
    static ContainerConfig from(DatabaseTest a) {
        return new ContainerConfig(
                a.version(), a.database(), a.username(), a.password(), a.initScripts()
        );
    }

    // Arrays don't participate in record equals/hashCode — override manually
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ContainerConfig c)) return false;
        return Objects.equals(version, c.version)
                && Objects.equals(database, c.database)
                && Objects.equals(username, c.username)
                && Objects.equals(password, c.password)
                && Arrays.equals(initScripts, c.initScripts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, database, username, password,
                Arrays.hashCode(initScripts));
    }
}