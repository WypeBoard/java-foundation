package io.github.wypeboard.foundation.database.test.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CleanDatabase {
    /**
     * The tables to truncate or delete from after each test.
     * <p>
     * At least one table must be specified. When {@link #disableConstraints()}
     * is {@code false}, tables are cleaned in the order declared — list child
     * tables before their parents to avoid foreign key violations.
     * <p>
     * Example:
     * <pre>{@code
     * @CleanDatabase(tables = {"order_items", "orders", "customers"})
     * }</pre>
     */
    String[] tables() default {};

    /**
     * Whether to disable foreign key constraint checks during cleanup.
     * <p>
     * When {@code true}, the extension sets {@code session_replication_role}
     * to {@code replica} for the duration of the cleanup, suppressing FK
     * trigger checks. This removes the need to declare tables in dependency
     * order but requires the test database user to be a superuser or table owner.
     * <p>
     * Defaults to {@code false}. When {@code false}, tables are cleaned in
     * the order declared in {@link #tables()} — ensure child tables are
     * listed before their parents.
     */
    boolean disableConstraints() default false;

    /**
     * The SQL operation used to clean each table.
     * <p>
     * Defaults to {@link TruncateMode#TRUNCATE}, which is faster and resets
     * sequences. Use {@link TruncateMode#DELETE} if your tables have triggers,
     * row-level security policies, or views that {@code TRUNCATE} bypasses.
     */
    TruncateMode mode() default TruncateMode.TRUNCATE;

    /**
     * The SQL operation to use when cleaning tables after each test.
     */
    enum TruncateMode {

        /**
         * Issues {@code TRUNCATE TABLE} for each table.
         * <p>
         * Faster than {@code DELETE} and resets identity sequences.
         * Does not fire row-level triggers.
         */
        TRUNCATE,

        /**
         * Issues {@code DELETE FROM} for each table.
         * <p>
         * Slower than {@code TRUNCATE} but fires row-level triggers
         * and respects row-level security policies. Prefer this when
         * the tables have triggers or views that depend on delete events.
         */
        DELETE
    }}
