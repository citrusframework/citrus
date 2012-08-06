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
import com.consol.citrus.TestActor;
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
    protected static final String SQL_COMMENT = "--";
    
    /** This actions explicit test actor */
    private TestActor actor;

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
     * Checks if this test action is disabled. Delegates to test actor defined
     * for this test action by default. Subclasses may add additional disabled logic here.
     * 
     * @param context the current test context.
     * @return
     */
    public boolean isDisabled(TestContext context) {
        return actor.isDisabled();
    }
    
    /**
     * Reads SQL statements from external file resource. File resource can hold several
     * multi-line statements and comments.
     * 
     * @param context the current test context.
     * @return list of SQL statements.
     */
    protected List<String> createStatementsFromFileResource(TestContext context) {
        BufferedReader reader = null;
        StringBuffer buffer;
        
        List<String> stmts = new ArrayList<String>();
        
        try {
            log.info("Executing SQL file: " + sqlResource.getFilename());
            
            reader = new BufferedReader(new InputStreamReader(sqlResource.getInputStream()));
            buffer = new StringBuffer();
            
            String line;
            while (reader.ready()) {
                line = reader.readLine();
    
                if (line != null && line.trim() != null && !line.trim().startsWith(SQL_COMMENT) && line.trim().length() > 0) {
                    if (line.trim().endsWith(getStatemendEndingCharacter())) {
                        buffer.append(decorateLastScriptLine(line));
                        String stmt = buffer.toString();
    
                        if (log.isDebugEnabled()) {
                            log.debug("Found statement: " + stmt);
                        }
    
                        stmts.add(context.replaceDynamicContentInString(stmt));
                        buffer.setLength(0);
                        buffer = new StringBuffer();
                    } else {
                        buffer.append(line);
                        
                        //more lines to come for this statement add line break
                        buffer.append("\n");
                    }
                }
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Resource could not be found - filename: " + sqlResource.getFilename(), e);
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
     * Gets the SQL statement ending character sequence.
     * @return
     */
    protected String getStatemendEndingCharacter() {
        return ";";
    }

    /**
     * Subclasses may want to decorate last script line.
     * @param line the last script line finishing a SQL statement.
     * @return
     */
    protected String decorateLastScriptLine(String line) {
        return line;
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

    /**
     * Gets the actor.
     * @return the actor the actor to get.
     */
    public TestActor getActor() {
        return actor;
    }

    /**
     * Sets the actor.
     * @param actor the actor to set
     */
    public void setActor(TestActor actor) {
        this.actor = actor;
    }
}
