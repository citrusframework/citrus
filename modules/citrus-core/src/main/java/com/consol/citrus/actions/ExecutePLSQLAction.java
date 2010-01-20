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
import java.text.ParseException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Executes PLSQL statements given inline or given by a file.
 *
 * @author deppisch Christoph Deppisch Consol*GmbH 2008
 *
 */
public class ExecutePLSQLAction extends AbstractDatabaseConnectingTestAction {
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(ExecutePLSQLAction.class);

    /** SQL file resource */
    private Resource sqlResource;

    /** In line script */
    private String script = null;

    /** boolean flag marking that possible SQL errors will be ignored */
    private boolean ignoreErrors = false;

    /** List of SQL statements */
    private List<String> statements = new ArrayList<String>();

    /**
     * @param statements the statements to set
     */
    public void setStatements(List<String> statements) {
        this.statements = statements;
    }

    /**
     * @see com.consol.citrus.TestAction#execute(TestContext)
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {
        BufferedReader reader = null;
        StringBuffer buffer;
        String stmt;

        try {
            if (script == null) {
                log.info("Executing PLSQL file: " + sqlResource.getFilename());

                reader = new BufferedReader(new InputStreamReader(sqlResource.getInputStream()));
                buffer = new StringBuffer();

                String line;
                while (reader.ready()) {
                    line = reader.readLine();

                    if(line != null) {
                        if (line.trim()!= null && line.trim().endsWith("/")) {
                            buffer.append(line.trim().substring(0, (line.trim().length() -1)));
    
                            stmt = context.replaceDynamicContentInString(buffer.toString());
                            statements.add(stmt);
                            buffer.setLength(0);
                            buffer = new StringBuffer();
                        } else {
                            buffer.append(line + "\n");
                        }
                    }
                }
            } else {
                script = context.replaceDynamicContentInString(script);
                if(log.isDebugEnabled()) {
                    log.debug("Found inline PLSQL script " + script);
                }

                StringTokenizer tok = new StringTokenizer(script, "/");
                while (tok.hasMoreTokens()) {
                    stmt = tok.nextToken();
                    statements.add(stmt.trim());
                }
            }

            for (int i = 0; i < statements.size(); i++) {
                try {
                    stmt = statements.get(i).toString();

                    if(log.isDebugEnabled()) {
                        log.debug("Executing SQL statement: " + stmt);
                    }
                    
                    getJdbcTemplate().execute(stmt);
                    log.info("SQL statement execution successful");
                } catch (Exception e) {
                    if (ignoreErrors) {
                        log.error("Error while executing SQL statement: " + e.getMessage());
                        continue;
                    } else {
                        throw new CitrusRuntimeException(e);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Resource could not be found - filename: " + sqlResource.getFilename(), e);
            throw new CitrusRuntimeException(e);
        } catch (ParseException e) {
            log.error("Error while parsing string", e);
            throw new CitrusRuntimeException(e);
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.warn("Warning: could not close reader instance", e);
                }
            }
        }
    }

    /**
     * Spring property setter.
     * @param statements
     */
    public void setScript(String script) {
        this.script = script;
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
