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

package com.consol.citrus.actions;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

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
    private boolean ignoreErrors = false;

    /**
     * Default constructor.
     */
    public ExecuteSQLAction() {
        setName("sql");
    }

    @Override
    public void doExecute(TestContext context) {
        if (statements.isEmpty()) {
            statements = createStatementsFromFileResource(context);
        }

        for (String stmt : statements)  {
            try {
                stmt = context.replaceDynamicContentInString(stmt.trim());

                if (stmt.endsWith(";")) {
                    stmt = stmt.substring(0, stmt.length()-1);
                }

                if (log.isDebugEnabled()) {
                    log.debug("Executing SQL statement: " + stmt);
                }

                getJdbcTemplate().execute(stmt);

                log.info("SQL statement execution successful");
            } catch (Exception e) {
                if (ignoreErrors) {
                    log.error("Error while executing statement " + stmt + " " + e.getLocalizedMessage());
                    continue;
                } else {
                    throw new CitrusRuntimeException(e);
                }
            }
        }
    }

    /**
     * Ignore errors during execution.
     * @param ignoreErrors boolean flag to set
     */
    public ExecuteSQLAction setIgnoreErrors(boolean ignoreErrors) {
        this.ignoreErrors = ignoreErrors;
        return this;
    }

    /**
     * Gets the ignoreErrors.
     * @return the ignoreErrors
     */
    public boolean isIgnoreErrors() {
        return ignoreErrors;
    }
}
