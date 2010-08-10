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

import java.io.*;
import java.text.ParseException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Class executes PLSQL statements either declared inline as PLSQL statements or given by an
 * external file resource.
 *
 * @author Christoph Deppisch
 * @since 2008
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
     * Setter for inline script.
     * @param script
     */
    public void setScript(String script) {
        this.script = script;
    }

    /**
     * Setterfor external file resource containing PLSLQ statements.
     * @param sqlResource
     */
    public void setSqlResource(Resource sqlResource) {
        this.sqlResource = sqlResource;
    }

    /**
     * Ignore errors during execution.
     * @param ignoreErrors boolean flag to set
     */
    public void setIgnoreErrors(boolean ignoreErrors) {
        this.ignoreErrors = ignoreErrors;
    }
}
