/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.schemahistory;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.database.Table;

/**
 * Factory to obtain a reference to the schema history.
 */
public class SchemaHistoryFactory {
    private SchemaHistoryFactory() {
        // Prevent instantiation
    }

    /**
     * Obtains a reference to the schema history.
     *
     * @param configuration The current Flyway configuration.
     * @param database     The Database object.
     * @param schema        The schema whose history to track.
     * @return The schema history.
     */
    public static SchemaHistory getSchemaHistory(FlywayConfiguration configuration, Database database, Schema schema
                                                 // [pro]
            , org.flywaydb.core.internal.util.jdbc.pro.DryRunStatementInterceptor dryRunStatementInterceptor
                                                 // [/pro]
    ) {
        String installedBy = configuration.getInstalledBy() == null
                ? database.getCurrentUser()
                : configuration.getInstalledBy();

        Table table = schema.getTable(configuration.getTable());
        JdbcTableSchemaHistory jdbcTableSchemaHistory = new JdbcTableSchemaHistory(database, table, installedBy);

        // [pro]
        if (configuration.getDryRunOutput() != null) {
            dryRunStatementInterceptor.init(database, table);
            return new org.flywaydb.core.internal.schemahistory.pro.InMemorySchemaHistory(jdbcTableSchemaHistory.exists(),
                    jdbcTableSchemaHistory.allAppliedMigrations(), installedBy, dryRunStatementInterceptor);
        }
        // [/pro]

        return jdbcTableSchemaHistory;
    }
}