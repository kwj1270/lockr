package com.official.lockr;

import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Component
public class DatabaseCleanup {

    private final DSLContext dslContext;

    public DatabaseCleanup(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Transactional(transactionManager = "transactionManager")
    public void execute() {
        try {
            dslContext.execute("SET FOREIGN_KEY_CHECKS = 0");

            final String currentDatabase = currentDatabase();
            final List<String> tableNames = getTableNames(currentDatabase);
            for (String tableName : tableNames) {
                dslContext.execute("TRUNCATE TABLE " + tableName);
                if (hasAutoIncrementColumn(tableName, currentDatabase)) {
                    dslContext.execute("ALTER TABLE " + tableName + " AUTO_INCREMENT = 1");
                }
            }
        } finally {
            dslContext.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    private String currentDatabase() {
        try {
            return Objects.requireNonNull(dslContext.configuration().connectionProvider().acquire()).getCatalog();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get current database", e);
        }
    }

    private List<String> getTableNames(String databaseName) {
        final Result<Record1<String>> result = dslContext
                .select(DSL.field("TABLE_NAME", String.class))
                .from("INFORMATION_SCHEMA.TABLES")
                .where(DSL.field("TABLE_SCHEMA").eq(databaseName))
                .and(DSL.field("TABLE_TYPE").eq("BASE TABLE")) // 뷰 제외
                .fetch();

        return result.getValues(0, String.class);
    }

    private boolean hasAutoIncrementColumn(String tableName, String databaseName) {
        final Integer count = dslContext.selectCount()
                .from("INFORMATION_SCHEMA.COLUMNS")
                .where(DSL.field("TABLE_SCHEMA").eq(databaseName))
                .and(DSL.field("TABLE_NAME").eq(tableName))
                .and(DSL.field("EXTRA").like("%auto_increment%"))
                .fetchOne(0, Integer.class);
        return Objects.nonNull(count) && count > 0;
    }
}
