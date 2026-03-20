package io.github.wypeboard.foundation.database.test.extension;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(PostgresExtension.class)
@SpringBootTest
public @interface DatabaseTest {

    /**
     * The Postgres Docker image tag to use.
     * <p>
     * Defaults to {@code 16-alpine}. Any valid tag from the official
     * {@code postgres} Docker Hub image is accepted, e.g. {@code 15-alpine},
     * {@code 14.9}, {@code latest}.
     */
    String version() default "16-alpine";

    /**
     * The name of the database to create inside the container.
     * <p>
     * Defaults to {@code testdb}. Each unique combination of annotation
     * attributes results in its own container, so modules that need
     * an isolated database should set a distinct name here.
     */
    String database() default "testdb";

    /**
     * The username used to authenticate with the database.
     * <p>
     * Defaults to {@code test}. Must match the credentials expected
     * by any {@code initScripts} that create roles or grant privileges.
     */
    String username() default "test";

    /**
     * The password used to authenticate with the database.
     * <p>
     * Defaults to {@code test}.
     */
    String password() default "test";

    /**
     * Classpath resources to execute against the database immediately
     * after the container starts, before any tests run.
     * <p>
     * Scripts are copied into {@code /docker-entrypoint-initdb.d/} and
     * executed in the order they are declared. Use this to set up schemas
     * or seed reference data when {@link #migrate()} is disabled.
     * <p>
     * Example:
     * <pre>{@code
     * @DatabaseTest(initScripts = {"sql/schema.sql", "sql/seed-data.sql"})
     * }</pre>
     */
    String[] initScripts() default {};

    /**
     * Whether to run database migrations on startup.
     * <p>
     * When {@code true} (the default), Flyway or Liquibase will run as
     * normal via Spring Boot's auto-configuration. Set to {@code false}
     * to suppress migration — useful when the schema is provided entirely
     * via {@link #initScripts()} or when testing migration logic explicitly.
     */
    boolean migrate() default true;
}
