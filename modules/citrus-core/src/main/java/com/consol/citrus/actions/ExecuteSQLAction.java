/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.actions;

import java.io.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Executes SQL statements that are embedded in the test or given by a file resource.
 * 
 * @author deppisch Christoph Deppisch, js Jan Szczepanski Consol*GmbH 2006
 *
 */
public class ExecuteSQLAction extends AbstractDatabaseConnectingTestAction {
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(ExecuteSQLAction.class);

    /** SQL file resource */
    private Resource sqlResource;

    /** List of SQL statements */
    private List<String> statements = new ArrayList<String>();

    /** Constant representing SQL comment */
    private static final String SQL_COMMENT = "--";

    /** boolean flag marking that possible SQL errors will be ignored */
    private boolean ignoreErrors = false;

    /**
     * @see com.consol.citrus.TestAction#execute(TestContext)
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {
        BufferedReader reader = null;
        String stmt = "";

        try {
            if (statements.isEmpty()) {
                log.info("Executing Sql file: " + sqlResource.getFilename());
                
                reader = new BufferedReader(new InputStreamReader(sqlResource.getInputStream()));
                StringBuffer buffer = new StringBuffer();
                String line;
                while (reader.ready()) {
                    line = reader.readLine();

                    if (line != null && line.trim() != null && !line.trim().startsWith(SQL_COMMENT) && line.trim().length() > 0) {
                        if (line.trim().endsWith(";")) {
                            buffer.append(line);
                            stmt = buffer.toString();
                            buffer.setLength(0);
                            buffer = new StringBuffer();

                            if(log.isDebugEnabled()) {
                                log.debug("Found statement: " + stmt);
                            }

                            statements.add(stmt);
                        } else {
                            buffer.append(line);
                        }
                    }
                }
            }

            Iterator<String> it = statements.iterator();
            while (it.hasNext())  {
                try {
                    stmt = it.next();
                    stmt = stmt.trim();

                    if (stmt.endsWith(";")) {
                        stmt = stmt.substring(0, stmt.length()-1);
                    }

                    stmt = context.replaceDynamicContentInString(stmt);

                    log.info("Found Sql statement " + stmt);
                    getJdbcTemplate().execute(stmt);
                } catch (Exception e) {
                    if (ignoreErrors) {
                        log.error("Error while executing statement " + stmt + " " + e.getLocalizedMessage());
                        continue;
                    } else {
                        throw new CitrusRuntimeException(e);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Sql resource could not be found - filename: "
                    + sqlResource.getFilename() + ". Nested Exception is: ");
            log.error(e.getLocalizedMessage());
            throw new CitrusRuntimeException(e);
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.warn("Warning: Error while closing reader instance", e);
                }
            }
        }
    }

    /**
     * Spring property setter.
     * @param statements
     */
    public void setStatements(List<String> statements) {
        this.statements = statements;
    }
    
    /**
     * Spring property setter.
     * @param sqlResource
     */
    public void setSqlResource(Resource sqlResource) {
        this.sqlResource = sqlResource;
    }

    /**
     * @param ignoreErrors the ignoreErrors to set
     */
    public void setIgnoreErrors(boolean ignoreErrors) {
        this.ignoreErrors = ignoreErrors;
    }
}
