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

import com.consol.citrus.TestAction;
import com.consol.citrus.TestActor;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.util.SqlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.util.ArrayList;
import java.util.List;

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
    
    /** SQL file resource path */
    protected String sqlResourcePath;
    
    /** List of SQL statements */
    protected List<String> statements = new ArrayList<>();
    
    /** This actions explicit test actor */
    private TestActor actor;

    /**
     * Do basic logging and delegate execution to subclass.
     */
    public void execute(TestContext context) {
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
        if (actor != null) {
            return actor.isDisabled();
        } else {
            return false;
        }
    }

    /**
     * Reads SQL statements from external file resource. File resource can hold several
     * multi-line statements and comments.
     *
     * @param context the current test context.
     * @return list of SQL statements.
     */
    protected List<String> createStatementsFromFileResource(TestContext context) {
        return SqlUtils.createStatementsFromFileResource(new PathMatchingResourcePatternResolver()
                .getResource(context.replaceDynamicContentInString(sqlResourcePath)));
    }

    /**
     * Reads SQL statements from external file resource. File resource can hold several
     * multi-line statements and comments.
     *
     * @param context the current test context.
     * @return list of SQL statements.
     */
    protected List<String> createStatementsFromFileResource(TestContext context, SqlUtils.LastScriptLineDecorator lineDecorator) {
        return SqlUtils.createStatementsFromFileResource(new PathMatchingResourcePatternResolver()
                .getResource(context.replaceDynamicContentInString(sqlResourcePath)), lineDecorator);
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
    public AbstractDatabaseConnectingTestAction setDescription(String description) {
        this.description = description;
        return this;
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
    public AbstractDatabaseConnectingTestAction setName(String name) {
        this.name = name;
        return this;
    }
    
    /**
     * List of statements to execute. Declared inline in the test case. 
     * @param statements
     */
    public AbstractDatabaseConnectingTestAction setStatements(List<String> statements) {
        this.statements = statements;
        return this;
    }
    
    /**
     * Setter for external file resource containing the SQL statements to execute.
     * @param sqlResource
     */
    public AbstractDatabaseConnectingTestAction setSqlResourcePath(String sqlResource) {
        this.sqlResourcePath = sqlResource;
        return this;
    }

    /**
     * Gets the sqlResource.
     * @return the sqlResource
     */
    public String getSqlResourcePath() {
        return sqlResourcePath;
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
    public AbstractDatabaseConnectingTestAction setActor(TestActor actor) {
        this.actor = actor;
        return this;
    }
}
