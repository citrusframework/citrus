/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.actions;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.List;

/**
 * Test action execute SQL statements. Use this action when executing
 * database altering statements like UPDATE, INSERT, ALTER, DELETE. Statements are either
 * embedded inline in the test case description or given by an external file resource.
 *
 * When executing SQL query statements (SELECT) see {@link ExecuteSQLQueryAction}.
 *
 * @author Christoph Deppisch, Jan Szczepanski
 * @since 2006
 */
public class ExecuteSQLAction extends AbstractDatabaseConnectingTestAction {

    /** boolean flag marking that possible SQL errors will be ignored */
    private final boolean ignoreErrors;

    /**
     * Default constructor.
     * @param builder
     */
    private ExecuteSQLAction(Builder builder) {
        super("sql", builder);

        this.ignoreErrors = builder.ignoreErrors;
    }

    @Override
    public void doExecute(TestContext context) {
        final List<String> statementsToUse;
        if (statements.isEmpty()) {
            statementsToUse = createStatementsFromFileResource(context);
        } else {
            statementsToUse = statements;
        }

        if (getTransactionManager() != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Using transaction manager: " + getTransactionManager().getClass().getName());
            }

            TransactionTemplate transactionTemplate = new TransactionTemplate(getTransactionManager());
            transactionTemplate.setTimeout(Integer.parseInt(context.replaceDynamicContentInString(getTransactionTimeout())));
            transactionTemplate.setIsolationLevelName(context.replaceDynamicContentInString(getTransactionIsolationLevel()));
            transactionTemplate.execute(status -> {
                executeStatements(statementsToUse, context);
                return null;
            });
        } else {
            executeStatements(statementsToUse, context);
        }
    }

    /**
     * Run all SQL statements.
     * @param statements
     * @param context
     */
    protected void executeStatements(List<String> statements, TestContext context) {
        if (getJdbcTemplate() == null) {
            throw new CitrusRuntimeException("No JdbcTemplate configured for sql execution!");
        }

        for (String statement : statements) {
            try {
                final String toExecute;

                if (statement.trim().endsWith(";")) {
                    toExecute = context.replaceDynamicContentInString(statement.trim().substring(0, statement.trim().length() - 1));
                } else {
                    toExecute = context.replaceDynamicContentInString(statement.trim());
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("Executing SQL statement: " + toExecute);
                }

                getJdbcTemplate().execute(toExecute);

                logger.info("SQL statement execution successful");
            } catch (Exception e) {
                if (ignoreErrors) {
                    logger.error("Ignoring error while executing SQL statement: " + e.getLocalizedMessage());
                } else {
                    throw new CitrusRuntimeException(e);
                }
            }
        }
    }

    /**
     * Gets the ignoreErrors.
     * @return the ignoreErrors
     */
    public boolean isIgnoreErrors() {
        return ignoreErrors;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractDatabaseConnectingTestAction.Builder<ExecuteSQLAction, Builder> {

        private boolean ignoreErrors = false;

        public static Builder sql() {
            return new Builder();
        }

        public static Builder sql(DataSource dataSource) {
            Builder builder = new Builder();
            builder.dataSource(dataSource);
            return builder;
        }

        public ExecuteSQLQueryAction.Builder query() {
            return new ExecuteSQLQueryAction.Builder().dataSource(dataSource);
        }

        /**
         * Ignore errors during execution.
         * @param ignoreErrors boolean flag to set
         */
        public Builder ignoreErrors(boolean ignoreErrors) {
            this.ignoreErrors = ignoreErrors;
            return this;
        }

        @Override
        public ExecuteSQLAction build() {
            return new ExecuteSQLAction(this);
        }
    }
}
