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

import java.util.List;
import javax.sql.DataSource;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ExecutePLSQLAction;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.springframework.transaction.PlatformTransactionManager;

public class Plsql implements TestActionBuilder<ExecutePLSQLAction>, ReferenceResolverAware {

    private final ExecutePLSQLAction.Builder builder = new ExecutePLSQLAction.Builder();

    private String dataSource;
    private String transactionManager;

    private ReferenceResolver referenceResolver;

    public void setDescription(String value) {
        builder.description(value);
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
        builder.name(String.format("plsql:%s", dataSource));
    }

    public void setStatements(List<Statement> statements) {
        for (Statement statement: statements) {
            if (statement.statement != null) {
                builder.statement(statement.statement);
            }

            if (statement.file != null) {
                builder.sqlResource(statement.file);
            }

            if (statement.script != null) {
                builder.sqlScript(statement.script);
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
        builder.ignoreErrors(value);
    }

    @Override
    public ExecutePLSQLAction build() {
        if (referenceResolver != null) {
            builder.dataSource(referenceResolver.resolve(dataSource, DataSource.class));

            if (transactionManager != null) {
                builder.transactionManager(referenceResolver.resolve(transactionManager, PlatformTransactionManager.class));
            }
        }

        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    public static class Statement {

        private String script;

        private String statement;

        protected String file;

        public String getScript() {
            return script;
        }

        public void setScript(String script) {
            this.script = script;
        }

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
}
