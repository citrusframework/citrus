/*
 * Copyright the original author or authors.
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

package org.citrusframework.sql.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.TestAction;
import org.citrusframework.TestActor;
import org.citrusframework.TestActorAware;
import org.citrusframework.api.actions.sql.DatabaseConnectingActionBuilder;
import org.citrusframework.api.common.Described;
import org.citrusframework.api.common.Named;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.sql.util.SqlStatementSource;
import org.citrusframework.sql.util.SqlUtils;
import org.citrusframework.util.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;

/**
 * Abstract base class for database connection test actions. Extends {@link JdbcDaoSupport} providing
 * access to a {@link javax.sql.DataSource}.
 *
 */
@SuppressWarnings("removal")
public abstract class AbstractDatabaseConnectingTestAction extends JdbcDaoSupport implements TestAction, Named, Described, TestActorAware {

    /** Text describing the test action */
    private String description;

    /** TestAction name injected as spring bean name */
    private String name;

    /** This actions explicit test actor */
    private TestActor actor;

    /** SQL file resource path */
    protected final String sqlResourcePath;

    /** SQL file resource paths in declaration order */
    protected final List<String> sqlResourcePaths;

    /** List of SQL statements */
    protected final List<String> statements;

    /** SQL statement sources in declaration order */
    protected final List<SqlStatementSource> sqlStatementSources;

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
        this.sqlResourcePaths = List.copyOf(builder.sqlResourcePaths);
        this.transactionIsolationLevel = builder.transactionIsolationLevel;
        this.transactionManager = builder.transactionManager;
        this.transactionTimeout = builder.transactionTimeout;
        this.statements = builder.statements;
        this.sqlStatementSources = List.copyOf(builder.sqlStatementSources);
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
     * Reads SQL statements from all configured external file resources, in declaration order.
     * File resources can hold several multi-line statements and comments.
     *
     * @param context the current test context.
     * @return list of SQL statements.
     */
    protected List<String> createStatementsFromFileResource(TestContext context) {
        return createStatementsFromFileResource(context, null);
    }

    /**
     * Reads SQL statements from all configured external file resources, in declaration order.
     * File resources can hold several multi-line statements and comments.
     *
     * @param context the current test context.
     * @return list of SQL statements.
     */
    protected List<String> createStatementsFromFileResource(TestContext context, SqlUtils.LastScriptLineDecorator lineDecorator) {
        List<String> stmts = new ArrayList<>();
        for (String resourcePath : sqlResourcePaths) {
            stmts.addAll(SqlUtils.createStatementsFromFileResource(resolveSqlResource(context, resourcePath), lineDecorator));
        }
        return stmts;
    }

    /**
     * Resolves all configured SQL statement sources to SQL statements, in declaration order.
     * Inline statements are used as is, external file resources are read and split into
     * statements and inline scripts are delegated to
     * {@link #createStatementsFromScript(TestContext, String)}.
     *
     * @param context the current test context.
     * @return list of SQL statements.
     */
    protected List<String> resolveStatements(TestContext context) {
        return resolveStatements(context, null);
    }

    /**
     * Resolves all configured SQL statement sources to SQL statements, in declaration order.
     * Inline statements are used as is, external file resources are read and split into
     * statements and inline scripts are delegated to
     * {@link #createStatementsFromScript(TestContext, String)}.
     *
     * @param context the current test context.
     * @param lineDecorator optional line decorator for last script lines of external file resources.
     * @return list of SQL statements.
     */
    protected List<String> resolveStatements(TestContext context, SqlUtils.LastScriptLineDecorator lineDecorator) {
        List<String> resolved = new ArrayList<>();
        for (SqlStatementSource source : sqlStatementSources) {
            switch (source) {
                case SqlStatementSource.Statement statement -> resolved.add(statement.sql());
                case SqlStatementSource.ResourcePath resourcePath -> resolved.addAll(
                        SqlUtils.createStatementsFromFileResource(resolveSqlResource(context, resourcePath.path()), lineDecorator));
                case SqlStatementSource.Script script -> resolved.addAll(createStatementsFromScript(context, script.script()));
            }
        }
        return resolved;
    }

    /**
     * Resolves an inline SQL script to SQL statements. Subclasses supporting inline scripts
     * (such as the PL/SQL action) overwrite this method.
     *
     * @param context the current test context.
     * @param script the inline SQL script.
     * @return list of SQL statements.
     */
    protected List<String> createStatementsFromScript(TestContext context, String script) {
        throw new CitrusRuntimeException("Inline SQL scripts are not supported by this test action");
    }

    /**
     * Resolves the given SQL resource path to a file resource, honoring resource prefixes
     * such as "classpath:" and "file:" as well as plain file system paths.
     *
     * @param context the current test context.
     * @param resourcePath the configured SQL resource path.
     * @return the resolved file resource.
     */
    private Resource resolveSqlResource(TestContext context, String resourcePath) {
        return Resources.create(context.replaceDynamicContentInString(resourcePath));
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
     * Gets all configured SQL file resource paths, in declaration order.
     * @return the SQL file resource paths
     */
    public List<String> getSqlResourcePaths() {
        return sqlResourcePaths;
    }

    /**
     * Gets the statements.
     * @return the statements
     */
    public List<String> getStatements() {
        return statements;
    }

    /**
     * Gets all configured SQL statement sources, in declaration order.
     * @return the SQL statement sources
     */
    public List<SqlStatementSource> getSqlStatementSources() {
        return sqlStatementSources;
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
    public static abstract class Builder<T extends AbstractDatabaseConnectingTestAction, S extends Builder<T, S>> extends AbstractTestActionBuilder<T, S>
            implements ReferenceResolverAware, DatabaseConnectingActionBuilder<T, S> {

        protected JdbcTemplate jdbcTemplate;
        protected DataSource dataSource;

        protected String dataSourceName;

        protected String sqlResourcePath;
        protected final List<String> sqlResourcePaths = new ArrayList<>();
        protected final List<String> statements = new ArrayList<>();
        protected final List<SqlStatementSource> sqlStatementSources = new ArrayList<>();
        protected PlatformTransactionManager transactionManager;
        protected String transactionTimeout = String.valueOf(TransactionDefinition.TIMEOUT_DEFAULT);
        protected String transactionIsolationLevel = "ISOLATION_DEFAULT";

        private ReferenceResolver referenceResolver;

        @Override
        public S jdbcTemplate(Object o) {
            if (o == null) {
                return self;
            }

            if (o instanceof JdbcTemplate template) {
                this.jdbcTemplate = template;
            } else {
                throw new CitrusRuntimeException("JdbcTemplate type '%s' is not supported".formatted(o.getClass().getName()));
            }
            return self;
        }

        @Override
        public S transactionManager(Object o) {
            if (o == null) {
                return self;
            }

            if (o instanceof PlatformTransactionManager manager) {
                this.transactionManager = manager;
            } else {
                throw new CitrusRuntimeException("Transaction manager type '%s' is not supported".formatted(o.getClass().getName()));
            }
            return self;
        }

        @Override
        public S transactionTimeout(int transactionTimeout) {
            this.transactionTimeout = String.valueOf(transactionTimeout);
            return self;
        }

        @Override
        public S transactionTimeout(String transactionTimeout) {
            this.transactionTimeout = transactionTimeout;
            return self;
        }

        @Override
        public S transactionIsolationLevel(String isolationLevel) {
            this.transactionIsolationLevel = isolationLevel;
            return self;
        }

        @Override
        public S dataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            return self;
        }

        @Override
        public S dataSource(String dataSourceName) {
            this.dataSourceName = dataSourceName;
            return self;
        }

        @Override
        public S statements(List<String> statements) {
            statements.forEach(this::statement);
            return self;
        }

        @Override
        public S statement(String sql) {
            return sqlStatementSource(new SqlStatementSource.Statement(sql));
        }

        @Override
        public S sqlResource(Resource sqlResource) {
            statements(SqlUtils.createStatementsFromFileResource(sqlResource));
            return self;
        }

        @Override
        public S sqlResource(String filePath) {
            return sqlStatementSource(new SqlStatementSource.ResourcePath(filePath));
        }

        /**
         * Adds a SQL statement source, keeping the order of declaration. Sources are either
         * inline statements, inline scripts or paths to external script resources.
         *
         * @param source the SQL statement source to add.
         * @return the builder.
         */
        public S sqlStatementSource(SqlStatementSource source) {
            this.sqlStatementSources.add(source);

            switch (source) {
                case SqlStatementSource.Statement statement -> this.statements.add(statement.sql());
                case SqlStatementSource.ResourcePath resourcePath -> {
                    if (this.sqlResourcePath == null) {
                        this.sqlResourcePath = resourcePath.path();
                    }
                    this.sqlResourcePaths.add(resourcePath.path());
                }
                case SqlStatementSource.Script ignored -> {
                    // inline scripts do not have a representation on this level
                }
            }

            return self;
        }

        @Override
        public final T build() {
            if (jdbcTemplate == null && dataSource == null && referenceResolver != null) {
                if (StringUtils.hasText(dataSourceName)) {
                    dataSource = referenceResolver.resolve(dataSourceName, DataSource.class);
                } else if (referenceResolver.isResolvable(DataSource.class)) {
                    dataSource = referenceResolver.resolve(DataSource.class);
                }
            }

            return doBuild();
        }

        protected abstract T doBuild();

        @Override
        public S withReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
            return self;
        }

        @Override
        public void setReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
        }
    }
}
