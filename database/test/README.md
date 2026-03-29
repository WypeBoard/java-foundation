# database/test

JUnit 5 extension that starts a real Postgres container for each unique database configuration and cleans up between tests. Built on Testcontainers and HikariCP. Integrates with Spring Boot's datasource auto-configuration when present.

---

## What's in here

### `@DatabaseTest`

Annotate a test class to get a Postgres container started before any tests run. The container is reused across all tests in the same JVM that share the same configuration — so multiple test classes with identical settings share one container.

```java
@DatabaseTest
class OrderRepositoryTest {
    // Spring picks up the datasource automatically
}
```

```java
@DatabaseTest(
    version      = "16-alpine",          // any postgres Docker Hub tag
    database     = "testdb",
    username     = "test",
    password     = "test",
    initScripts  = {"sql/schema.sql", "sql/seed-data.sql"},
    migrate      = true                  // set false to suppress Flyway/Liquibase
)
class MyTest { ... }
```

#### What it does

- Starts a `postgres:{version}` Testcontainers container.
- Copies `initScripts` classpath resources into `/docker-entrypoint-initdb.d/` in declaration order.
- Sets `spring.datasource.url/username/password` system properties so Spring Boot's `DataSourceAutoConfiguration` picks them up without any extra configuration.
- When `migrate = false`, also sets `spring.flyway.enabled=false` and `spring.liquibase.enabled=false`.
- Exposes the `DataSource` as a JUnit 5 parameter — inject it directly into test methods or constructors if you are not using Spring.

```java
@DatabaseTest(migrate = false, initScripts = "sql/schema.sql")
class RawJdbcTest {

    @Test
    void insertsRow(DataSource ds) throws Exception {
        try (var conn = ds.getConnection();
             var stmt = conn.prepareStatement("INSERT INTO orders(id) VALUES (?)")) {
            stmt.setInt(1, 1);
            stmt.executeUpdate();
        }
    }
}
```

---

### `@CleanDatabase`

Truncates or deletes from a set of tables after each test. Apply at class level (affects every test) or method level (overrides the class-level setting for that test).

```java
@DatabaseTest
@CleanDatabase(tables = {"order_items", "orders", "customers"})
class OrderServiceTest {
    // after each test: truncates order_items, then orders, then customers
}
```

#### Options

```java
@CleanDatabase(
    tables             = {"order_items", "orders"},
    mode               = TruncateMode.TRUNCATE,   // or DELETE
    disableConstraints = false                     // set true to skip FK checks
)
```

| Option | Default | Notes |
|---|---|---|
| `tables` | *(required)* | At least one table must be specified |
| `mode` | `TRUNCATE` | `TRUNCATE` is faster and resets sequences; `DELETE` fires row-level triggers |
| `disableConstraints` | `false` | When `true`, sets `session_replication_role = 'replica'` for the cleanup — removes the need to order tables by FK dependency, but requires superuser or table owner |

When `disableConstraints = false`, list child tables before their parents to avoid FK violations.

---

## Dependency

```kotlin
testImplementation("io.github.wypeboard.foundation:test-database:0.1.0")
```

You also need to bring Testcontainers and HikariCP on the test classpath (they are `compileOnly` in this module):

```kotlin
testImplementation("org.testcontainers:postgresql:1.21.4")
testImplementation("com.zaxxer:HikariCP:7.0.2")
```

For Spring Boot integration:

```kotlin
testImplementation("org.springframework.boot:spring-boot-starter-test")
```

Docker must be available on the machine running the tests.