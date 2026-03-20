package io.github.wypeboard.foundation.database.test.extension;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.wypeboard.foundation.utils.test.extension.ExtentionHelper;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PostgresExtension
        implements BeforeAllCallback, AfterEachCallback, ParameterResolver {

    private static final Map<ContainerConfig, PostgreSQLContainer<?>> CONTAINERS
            = new ConcurrentHashMap<>();
    private static final Map<ContainerConfig, DataSource> DATA_SOURCES
            = new ConcurrentHashMap<>();

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    public void beforeAll(ExtensionContext context) {
        ExtentionHelper.retrieveAnnotationFromTestClasses(DatabaseTest.class, context).ifPresent(settings -> {
            ContainerConfig config  = ContainerConfig.from(settings);

            PostgreSQLContainer<?> container = CONTAINERS.computeIfAbsent(
                    config, PostgresExtension::startContainer
            );

            DATA_SOURCES.computeIfAbsent(
                    config, k -> buildDataSource(container)
            );

            // Let Spring pick these up — no-op if Spring isn't bootstrapping a context
            System.setProperty("spring.datasource.url",      container.getJdbcUrl());
            System.setProperty("spring.datasource.username", container.getUsername());
            System.setProperty("spring.datasource.password", container.getPassword());

            if (!settings.migrate()) {
                System.setProperty("spring.flyway.enabled",    "false");
                System.setProperty("spring.liquibase.enabled", "false");
            }
        });
    }

    @Override
    public void afterEach(ExtensionContext context) {
        // Method-level annotation wins over class-level
        ExtentionHelper.retrieveAnnotationFromTestClasses(CleanDatabase.class, context)
                .ifPresent(clean -> {
                    DatabaseTest databaseTest = ExtentionHelper.retrieveAnnotationFromTestClasses(DatabaseTest.class, context)
                            .orElseThrow(() -> new IllegalStateException(
                                    "Could not find @DatabaseTest on test class or any enclosing context"
                            ));
                    DataSource ds = DATA_SOURCES.get(ContainerConfig.from(databaseTest));
                    cleanTables(ds, clean);
                });
    }

    // -------------------------------------------------------------------------
    // ParameterResolver — inject DataSource into test methods/constructors
    // -------------------------------------------------------------------------

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == DataSource.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext,
                                   ExtensionContext extensionContext) {
        DatabaseTest databaseTest = ExtentionHelper.retrieveAnnotationFromTestClasses(DatabaseTest.class, extensionContext)
                .orElseThrow(() -> new IllegalStateException(
                        "Could not find @DatabaseTest on test class or any enclosing context"
                ));
        ContainerConfig config = ContainerConfig.from(databaseTest);
        return DATA_SOURCES.get(config);
    }

    // -------------------------------------------------------------------------
    // Container startup
    // -------------------------------------------------------------------------

    private static PostgreSQLContainer<?> startContainer(ContainerConfig config) {
        PostgreSQLContainer<?> container =
                new PostgreSQLContainer<>("postgres:" + config.version())
                        .withDatabaseName(config.database())
                        .withUsername(config.username())
                        .withPassword(config.password());

        Arrays.stream(config.initScripts())
                .map(MountableFile::forClasspathResource)
                .forEach(script ->
                        container.withCopyFileToContainer(
                                script, "/docker-entrypoint-initdb.d/"
                        )
                );

        container.start();
        return container;
    }

    private static DataSource buildDataSource(PostgreSQLContainer<?> container) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(container.getJdbcUrl());
        config.setUsername(container.getUsername());
        config.setPassword(container.getPassword());
        config.setMaximumPoolSize(5);
        return new HikariDataSource(config);
    }

    // -------------------------------------------------------------------------
    // Table cleanup
    // -------------------------------------------------------------------------

    private void cleanTables(DataSource dataSource, CleanDatabase clean) {
        if (clean.tables().length == 0) {
            throw new IllegalStateException(
                    "@CleanDatabase requires at least one table to be specified"
            );
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            if (clean.disableConstraints()) {
                cleanWithConstraintsDisabled(conn, clean);
            } else {
                cleanOrdered(conn, clean);
            }

            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clean database after test", e);
        }
    }

    private void cleanOrdered(Connection conn, CleanDatabase clean) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            for (String table : clean.tables()) {
                stmt.execute(buildCleanStatement(table, clean.mode(), false));
            }
        }
    }

    private void cleanWithConstraintsDisabled(Connection conn,
                                              CleanDatabase clean) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Suppresses FK trigger checks for this session
            stmt.execute("SET session_replication_role = 'replica'");
            for (String table : clean.tables()) {
                stmt.execute(buildCleanStatement(table, clean.mode(), false));
            }
            stmt.execute("SET session_replication_role = 'origin'");
        }
    }

    private String buildCleanStatement(String table,
                                       CleanDatabase.TruncateMode mode,
                                       boolean cascade) {
        return switch (mode) {
            case TRUNCATE -> "TRUNCATE TABLE " + table + (cascade ? " CASCADE" : "");
            case DELETE   -> "DELETE FROM "    + table;
        };
    }
}