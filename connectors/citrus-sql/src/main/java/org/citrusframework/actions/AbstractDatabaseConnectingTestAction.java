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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.TestAction;
import org.citrusframework.TestActor;
import org.citrusframework.TestActorAware;
import org.citrusframework.common.Described;
import org.citrusframework.common.Named;
import org.citrusframework.context.TestContext;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.SqlUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;

/**
 * Abstract base class for database connection test actions. Extends {@link JdbcDaoSupport} providing
 * access to a {@link javax.sql.DataSource}.
 *
 * @author Christoph Deppisch
 */
public abstract class AbstractDatabaseConnectingTestAction extends JdbcDaoSupport implements TestAction, Named, Described, TestActorAware {

    /** Text describing the test action */
    private String description;

    /** TestAction name injected as spring bean name */
    private String name;

    /** This actions explicit test actor */
    private TestActor actor;

    /** SQL file resource path */
    protected final String sqlResourcePath;

    /** List of SQL statements */
    protected final List<String> statements;

    /** Optional transaction manager */
    private final PlatformTransactionManager transactionManager;
    private final String transactionTimeout;
    private final String transactionIsolationLevel;

    protected AbstractDatabaseConnectingTestAction(String name, Builder<?, ?> builder) {
        this.name = Optional.ofNullable(builder.getName()).orElse(name);
        this.description = builder.getDescription();
        this.actor = builder.getActor();

        Optional.ofNullable(builder.jdbcTemplate).ifPresent(super::setJdbcTemplate);
        Optional.ofNullable(builder.dataSource).ifPresent(super::setDataSource);

        this.sqlResourcePath = builder.sqlResourcePath;
        this.transactionIsolationLevel = builder.transactionIsolationLevel;
        this.transactionManager = builder.transactionManager;
        this.transactionTimeout = builder.transactionTimeout;
        this.statements = builder.statements;
    }

    /**
     * Do basic logging and delegate execution to subclass.
     */
    public void execute(TestContext context) {
        if (!isDisabled(context)) {
            doExecute(context);
        }
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
        return SqlUtils.createStatementsFromFileResource(Resources.fromClasspath(context.replaceDynamicContentInString(sqlResourcePath)));
    }

    /**
     * Reads SQL statements from external file resource. File resource can hold several
     * multi-line statements and comments.
     *
     * @param context the current test context.
     * @return list of SQL statements.
     */
    protected List<String> createStatementsFromFileResource(TestContext context, SqlUtils.LastScriptLineDecorator lineDecorator) {
        return SqlUtils.createStatementsFromFileResource(Resources.fromClasspath(context.replaceDynamicContentInString(sqlResourcePath)), lineDecorator);
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public AbstractDatabaseConnectingTestAction setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
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
     * Gets the transactionManager.
     *
     * @return
     */
    public PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    /**
     * Gets the transactionTimeout.
     *
     * @return
     */
    public String getTransactionTimeout() {
        return transactionTimeout;
    }

    /**
     * Gets the transactionIsolationLevel.
     *
     * @return
     */
    public String getTransactionIsolationLevel() {
        return transactionIsolationLevel;
    }

    @Override
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

    /**
     * Action builder.
     */
    public static abstract class Builder<T extends AbstractDatabaseConnectingTestAction, S extends Builder<T, S>> extends AbstractTestActionBuilder<T, S> {

        protected JdbcTemplate jdbcTemplate;
        protected DataSource dataSource;
        protected String sqlResourcePath;
        protected final List<String> statements = new ArrayList<>();
        protected PlatformTransactionManager transactionManager;
        protected String transactionTimeout = String.valueOf(TransactionDefinition.TIMEOUT_DEFAULT);
        protected String transactionIsolationLevel = "ISOLATION_DEFAULT";

        /**
         * Sets the Spring JDBC template to use.
         * @param jdbcTemplate
         * @return
         */
        public S jdbcTemplate(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
            return self;
        }

        /**
         * Sets the transaction manager to use.
         * @param transactionManager
         * @return
         */
        public S transactionManager(PlatformTransactionManager transactionManager) {
            this.transactionManager = transactionManager;
            return self;
        }

        /**
         * Sets the transaction timeout to use.
         * @param transactionTimeout
         * @return
         */
        public S transactionTimeout(int transactionTimeout) {
            this.transactionTimeout = String.valueOf(transactionTimeout);
            return self;
        }

        /**
         * Sets the transaction timeout to use.
         * @param transactionTimeout
         * @return
         */
        public S transactionTimeout(String transactionTimeout) {
            this.transactionTimeout = transactionTimeout;
            return self;
        }

        /**
         * Sets the transaction isolation level to use.
         * @param isolationLevel
         * @return
         */
        public S transactionIsolationLevel(String isolationLevel) {
            this.transactionIsolationLevel = isolationLevel;
            return self;
        }

        /**
         * Sets the SQL data source.
         * @param dataSource
         * @return
         */
        public S dataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            return self;
        }

        /**
         * List of statements to execute. Declared inline in the test case.
         * @param statements
         */
        public S statements(List<String> statements) {
            this.statements.addAll(statements);
            return self;
        }

        /**
         * Adds a new statement to the list of SQL executions.
         * @param sql
         * @return
         */
        public S statement(String sql) {
            this.statements.add(sql);
            return self;
        }

        /**
         * Setter for external file resource containing the SQL statements to execute.
         * @param sqlResource
         */
        public S sqlResource(Resource sqlResource) {
            statements(SqlUtils.createStatementsFromFileResource(sqlResource));
            return self;
        }

        /**
         * Setter for external file resource containing the SQL statements to execute.
         * @param filePath
         */
        public S sqlResource(String filePath) {
            this.sqlResourcePath = filePath;
            return self;
        }
    }
}
