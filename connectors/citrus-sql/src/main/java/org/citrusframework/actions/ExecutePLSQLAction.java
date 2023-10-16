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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.sql.DataSource;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.SqlUtils;
import org.citrusframework.util.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Class executes PLSQL statements either declared inline as PLSQL statements or given by an
 * external file resource.
 *
 * @author Christoph Deppisch
 * @since 2008
 */
public class ExecutePLSQLAction extends AbstractDatabaseConnectingTestAction {
    /** In line script */
    private final String script;

    /** boolean flag marking that possible SQL errors will be ignored */
    private final boolean ignoreErrors;

    /** Special statement endoing character sequence */
    public static final String PLSQL_STMT_ENDING = "/";

    /**
     * Default constructor.
     */
    public ExecutePLSQLAction(Builder builder) {
        super("plsql", builder);

        this.ignoreErrors = builder.ignoreErrors;
        this.script = builder.script;
    }

    @Override
    public void doExecute(TestContext context) {
        List<String> statementsToUse;
        if (StringUtils.hasText(script)) {
            statementsToUse = createStatementsFromScript(context);
        } else if (StringUtils.hasText(sqlResourcePath)) {
            statementsToUse = createStatementsFromFileResource(context, new SqlUtils.LastScriptLineDecorator() {
                @Override
                public String getStatementEndingCharacter() {
                    return PLSQL_STMT_ENDING;
                }

                @Override
                public String decorate(String line) {
                    return line.trim().substring(0, (line.trim().length() - 1));
                }
            });
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
     * Run all PLSQL statements.
     * @param statements
     * @param context
     */
    protected void executeStatements(List<String> statements, TestContext context) {
        if (getJdbcTemplate() == null) {
            throw new CitrusRuntimeException("No JdbcTemplate configured for sql execution!");
        }

        for (String statement : statements) {
            try {
                final String toExecute = context.replaceDynamicContentInString(statement.trim());

                if (logger.isDebugEnabled()) {
                    logger.debug("Executing PLSQL statement: " + toExecute);
                }

                getJdbcTemplate().execute(toExecute);

                logger.info("PLSQL statement execution successful");
            } catch (DataAccessException e) {
                if (ignoreErrors) {
                    logger.warn("Ignoring error while executing PLSQL statement: " + e.getMessage());
                } else {
                    throw new CitrusRuntimeException("Failed to execute PLSQL statement", e);
                }
            }
        }
    }

    /**
     * Create SQL statements from inline script.
     * @param context the current test context.
     * @return list of SQL statements.
     */
    private List<String> createStatementsFromScript(TestContext context) {
        List<String> stmts = new ArrayList<>();

        String resolvedScript = context.replaceDynamicContentInString(script);
        if (logger.isDebugEnabled()) {
            logger.debug("Found inline PLSQL script " + resolvedScript);
        }

        StringTokenizer tok = new StringTokenizer(resolvedScript, PLSQL_STMT_ENDING);
        while (tok.hasMoreTokens()) {
            String next = tok.nextToken().trim();
            if (StringUtils.hasText(next)) {
                stmts.add(next);
            }
        }

        return stmts;
    }

    /**
     * Gets the script.
     * @return the script
     */
    public String getScript() {
        return script;
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
    public static final class Builder extends AbstractDatabaseConnectingTestAction.Builder<ExecutePLSQLAction, Builder> {

        private boolean ignoreErrors = false;
        private String script;

        public static Builder plsql() {
            return new Builder();
        }

        public static Builder plsql(DataSource dataSource) {
            Builder builder = new Builder();
            builder.dataSource(dataSource);
            return builder;
        }

        /**
         * Setter for inline script.
         * @param script
         */
        public Builder sqlScript(String script) {
            this.script = script;
            return this;
        }

        /**
         * Setter for external file resource containing the SQL statements to execute.
         * @param sqlResource
         */
        public Builder sqlResource(Resource sqlResource) {
            return sqlResource(sqlResource, FileUtils.getDefaultCharset());
        }

        /**
         * Setter for external file resource containing the SQL statements to execute.
         * @param sqlResource
         * @param charset
         */
        public Builder sqlResource(Resource sqlResource, Charset charset) {
            try {
                sqlScript(FileUtils.readToString(sqlResource, charset));
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to read sql resource", e);
            }
            return this;
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
        public ExecutePLSQLAction build() {
            return new ExecutePLSQLAction(this);
        }
    }
}
