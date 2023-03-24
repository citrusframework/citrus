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

package org.citrusframework.sql.xml;

import javax.sql.DataSource;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ExecutePLSQLAction;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.springframework.transaction.PlatformTransactionManager;

@XmlRootElement(name = "plsql")
public class Plsql implements TestActionBuilder<ExecutePLSQLAction>, ReferenceResolverAware {

    private final ExecutePLSQLAction.Builder builder = new ExecutePLSQLAction.Builder();

    private String dataSource;
    private String transactionManager;

    private ReferenceResolver referenceResolver;

    @XmlElement
    public Plsql setDescription(String value) {
        builder.description(value);
        return this;
    }

    @XmlAttribute(name = "datasource", required = true)
    public Plsql setDataSource(String dataSource) {
        this.dataSource = dataSource;
        builder.name(String.format("plsql:%s", dataSource));
        return this;
    }

    @XmlElement(required = true)
    public Plsql setStatements(Statements statements) {
        statements.getStatements().forEach(builder::statement);

        if (statements.file != null) {
            builder.sqlResource(statements.file);
        }

        if (statements.script != null) {
            builder.sqlScript(statements.script);
        }

        return this;
    }

    @XmlElement(name = "transaction")
    public Plsql setTransaction(Transaction transaction) {
        if (transaction.manager != null) {
            transactionManager = transaction.manager;
        }

        if (transaction.isolationLevel != null) {
            builder.transactionIsolationLevel(transaction.isolationLevel);
        }

        if (transaction.timeout != null) {
            builder.transactionTimeout(transaction.timeout);
        }

        return this;
    }

    @XmlAttribute(name = "ignore-errors")
    public Plsql setIgnoreErrors(boolean value) {
        builder.ignoreErrors(value);
        return this;
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

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "statements",
            "script"
    })
    public static class Statements {

        @XmlElement(name = "script")
        private String script;

        @XmlElement(name = "statement")
        private List<String> statements;

        @XmlAttribute(name = "file")
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

        public List<String> getStatements() {
            if (statements == null) {
                statements = new ArrayList<>();
            }
            return statements;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Transaction {

        @XmlAttribute(name = "manager")
        protected String manager;
        @XmlAttribute(name = "timeout")
        protected String timeout;
        @XmlAttribute(name = "isolation-level")
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
