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
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.consol.citrus.TestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.TestActionExecutionLogger;

/**
 * Abstract base class for database connection test actions. Extends {@link JdbcDaoSupport} providing
 * access to a {@link javax.sql.DataSource}.
 * 
 * @author Christoph Deppisch
 */
public abstract class AbstractDatabaseConnectingTestAction extends JdbcDaoSupport implements TestAction {
    /**
     * Logger
     */
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /** Text describing the test action */
    private String description;

    /** TestAction name injected as spring bean name */
    private String name = this.getClass().getSimpleName();
    
    /** SQL file resource */
    protected Resource sqlResource;
    
    /** List of SQL statements */
    protected List<String> statements = new ArrayList<String>();
    
    /** Constant representing SQL comment */
    private static final String SQL_COMMENT = "--";

    /**
     * Do basic logging and delegate execution to subclass.
     */
    public void execute(TestContext context) {
        TestActionExecutionLogger.logTestAction(this);
        
        doExecute(context);
    }
    
    /**
     * Subclasses may add custom execution logic here.
     */
    public abstract void doExecute(TestContext context);

    /**
     * Reads SQL statements from external file resource. File resource can hold several
     * multi-line statements and comments.
     * 
     * @return list of SQL statements.
     */
    protected List<String> getStatementsFromResource() {
        BufferedReader reader = null;
        StringBuffer buffer = new StringBuffer();
        String line = "";
        String stmt = "";
        
        List<String> stmts = new ArrayList<String>();
        
        log.info("Executing Sql file: " + sqlResource.getFilename());
        
        try {
            reader = new BufferedReader(new InputStreamReader(sqlResource.getInputStream()));
            while (reader.ready()) {
                line = reader.readLine();
    
                if (line != null && line.trim() != null && !line.trim().startsWith(SQL_COMMENT) && line.trim().length() > 0) {
                    buffer.append(line);
                    
                    if (line.trim().endsWith(";")) {
                        stmt = buffer.toString();
                        buffer.setLength(0);
                        buffer = new StringBuffer();
    
                        if (log.isDebugEnabled()) {
                            log.debug("Found statement: " + stmt);
                        }
    
                        stmts.add(stmt);
                    } else {
                        //more lines to some for this statement add line break
                        buffer.append("\n");
                    }
                }
            }
        } catch (IOException e) {
            log.error("Sql resource could not be found - filename: "
                    + sqlResource.getFilename() + ". Nested Exception is: ");
            log.error(e.getLocalizedMessage());
            throw new CitrusRuntimeException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.warn("Warning: Error while closing reader instance", e);
                }
            }
        }
        
        return stmts;
    }
    
    /**
     * Gets this action's description.
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets this test action's description.
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the name of the test action.
     * @return the test action name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets this test action's name.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * List of statements to execute. Declared inline in the test case. 
     * @param statements
     */
    public void setStatements(List<String> statements) {
        this.statements = statements;
    }
    
    /**
     * Setter for external file resource containing the SQL statements to execute.
     * @param sqlResource
     */
    public void setSqlResource(Resource sqlResource) {
        this.sqlResource = sqlResource;
    }

    /**
     * Gets the sqlResource.
     * @return the sqlResource
     */
    public Resource getSqlResource() {
        return sqlResource;
    }

    /**
     * Gets the statements.
     * @return the statements
     */
    public List<String> getStatements() {
        return statements;
    }
}
