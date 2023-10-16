/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.sql.yaml;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.AbstractDatabaseConnectingTestAction;
import org.citrusframework.actions.ExecuteSQLAction;
import org.citrusframework.actions.ExecuteSQLQueryAction;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;

public class Sql implements TestActionBuilder<AbstractDatabaseConnectingTestAction>, ReferenceResolverAware {

    private AbstractDatabaseConnectingTestAction.Builder<?, ?> builder = new ExecuteSQLAction.Builder();

    private String dataSource;
    private String transactionManager;

    private ReferenceResolver referenceResolver;

    protected List<Validate> validate;

    protected List<Extract> extract;

    public void setDescription(String value) {
        builder.description(value);
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
        builder.name(String.format("sql:%s", dataSource));
    }

    public void setStatements(List<Statement> statements) {
        for (Statement statement : statements) {
            if (statement.statement != null) {
                builder.statement(statement.statement);
            }

            if (statement.file != null) {
                builder.sqlResource(statement.file);
            }
        }
    }

    public void setTransaction(Transaction transaction) {
        if (transaction.manager != null) {
            transactionManager = transaction.manager;
        }

        if (transaction.isolationLevel != null) {
            builder.transactionIsolationLevel(transaction.isolationLevel);
        }

        if (transaction.timeout != null) {
            builder.transactionTimeout(transaction.timeout);
        }
    }

    public void setIgnoreErrors(boolean value) {
        if (builder instanceof ExecuteSQLAction.Builder) {
            ((ExecuteSQLAction.Builder) builder).ignoreErrors(value);
        }
    }

    public List<Validate> getValidates() {
        if (validate == null) {
            validate = new ArrayList<>();
        }
        return this.validate;
    }

    public void setValidate(List<Validate> validate) {
        this.validate = validate;
    }

    public List<Extract> getExtracts() {
        if (extract == null) {
            extract = new ArrayList<>();
        }
        return this.extract;
    }

    public void setExtract(List<Extract> extract) {
        this.extract = extract;
    }

    @Override
    public AbstractDatabaseConnectingTestAction build() {
        if (referenceResolver != null) {
            builder.dataSource(referenceResolver.resolve(dataSource, DataSource.class));

            if (transactionManager != null) {
                builder.transactionManager(referenceResolver.resolve(transactionManager, PlatformTransactionManager.class));
            }
        }

        for (Validate validate : getValidates()) {
            if (validate.column != null) {
                if (validate.value != null) {
                    asSqlQueryBuilder().validate(validate.column, validate.value);
                }

                if (validate.getValues() != null) {
                    asSqlQueryBuilder().validate(validate.column, validate.getValues().toArray(String[]::new));
                }
            }

            Validate.Script script = validate.script;
            // check for nested validate script child node
            if (script != null) {
                String type = script.getType();

                String filePath = script.getFile();
                if (StringUtils.hasText(filePath)) {
                    if (script.getCharset() != null) {
                        asSqlQueryBuilder().validateScriptResource(filePath, type, Charset.forName(script.getCharset()));
                    } else {
                        asSqlQueryBuilder().validateScriptResource(filePath, type, StandardCharsets.UTF_8);
                    }
                } else if (script.getValue() != null) {
                    asSqlQueryBuilder().validateScript(script.getValue().trim(), type);
                }
            }
        }

        for (Extract extract : getExtracts()) {
            asSqlQueryBuilder().extract(extract.column, extract.variable);
        }

        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    /**
     * Converts sql action builder to query action builder.
     * @return
     */
    private ExecuteSQLQueryAction.Builder asSqlQueryBuilder() {
        if (builder instanceof ExecuteSQLQueryAction.Builder) {
            return (ExecuteSQLQueryAction.Builder) builder;
        }

        ExecuteSQLQueryAction.Builder sqlQueryBuilder = new ExecuteSQLQueryAction.Builder();

        AbstractDatabaseConnectingTestAction base = builder.build();
        sqlQueryBuilder.description(base.getDescription());
        sqlQueryBuilder.dataSource(base.getDataSource());
        sqlQueryBuilder.jdbcTemplate(base.getJdbcTemplate());
        sqlQueryBuilder.sqlResource(base.getSqlResourcePath());
        sqlQueryBuilder.statements(base.getStatements());

        sqlQueryBuilder.transactionManager(base.getTransactionManager());
        sqlQueryBuilder.transactionTimeout(base.getTransactionTimeout());
        sqlQueryBuilder.transactionIsolationLevel(base.getTransactionIsolationLevel());

        sqlQueryBuilder.actor(base.getActor());

        builder = sqlQueryBuilder;
        return sqlQueryBuilder;
    }

    public static class Statement {

        private String statement;

        private String file;

        public String getFile() {
            return file;
        }

        public void setFile(String value) {
            this.file = value;
        }

        public String getStatement() {
            return statement;
        }

        public void setStatement(String statement) {
            this.statement = statement;
        }
    }

    public static class Transaction {

        protected String manager;
        protected String timeout;
        protected String isolationLevel;

        public String getManager() {
            return manager;
        }

        public void setManager(String manager) {
            this.manager = manager;
        }

        public String getTimeout() {
            return timeout;
        }

        public void setTimeout(String timeout) {
            this.timeout = timeout;
        }

        public String getIsolationLevel() {
            return isolationLevel;
        }

        public void setIsolationLevel(String isolationLevel) {
            this.isolationLevel = isolationLevel;
        }
    }

    public static class Extract {

        protected String column;
        protected String variable;

        public String getColumn() {
            return column;
        }

        public void setColumn(String value) {
            this.column = value;
        }

        public String getVariable() {
            return variable;
        }

        public void setVariable(String value) {
            this.variable = value;
        }
    }

    public static class Validate {

        protected List<String> values;
        protected String column;
        protected String value;
        protected Script script;

        public List<String> getValues() {
            return values;
        }

        public void setValues(List<String> value) {
            this.values = value;
        }

        public String getColumn() {
            return column;
        }

        public void setColumn(String value) {
            this.column = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Script getScript() {
            return script;
        }

        public void setScript(Script script) {
            this.script = script;
        }

        public static class Script {

            protected String value;
            protected String type;
            protected String file;
            protected String charset;

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }

            public String getType() {
                return type;
            }

            public void setType(String value) {
                this.type = value;
            }

            public String getFile() {
                return file;
            }

            public void setFile(String value) {
                this.file = value;
            }

            public void setCharset(String charset) {
                this.charset = charset;
            }

            public String getCharset() {
                return charset;
            }
        }
    }
}
