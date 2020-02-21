/*
 * Copyright 2006-2018 the original author or authors.
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

package com.consol.citrus.jdbc.generator;

import com.consol.citrus.jdbc.model.CloseConnection;
import com.consol.citrus.jdbc.model.CloseStatement;
import com.consol.citrus.jdbc.model.CreateCallableStatement;
import com.consol.citrus.jdbc.model.CreatePreparedStatement;
import com.consol.citrus.jdbc.model.CreateStatement;
import com.consol.citrus.jdbc.model.Execute;
import com.consol.citrus.jdbc.model.OpenConnection;
import com.consol.citrus.jdbc.model.Operation;
import com.consol.citrus.jdbc.model.TransactionCommitted;
import com.consol.citrus.jdbc.model.TransactionRollback;
import com.consol.citrus.jdbc.model.TransactionStarted;

public class JdbcOperationGenerator {

    public Operation generateOpenConnection(final OpenConnection openConnection){
        final Operation operation = new Operation();
        operation.setOpenConnection(openConnection);
        return operation;
    }

    public Operation generateCloseConnection() {
        final Operation operation = new Operation();
        operation.setCloseConnection(new CloseConnection());
        return operation;
    }

    public Operation generatePreparedStatement(final String sql) {
        final Operation operation = new Operation();

        final CreatePreparedStatement createPreparedStatement = new CreatePreparedStatement();
        createPreparedStatement.setSql(sql);

        operation.setCreatePreparedStatement(createPreparedStatement);
        return operation;
    }

    public Operation generateCreateStatement() {
        final Operation operation = new Operation();
        operation.setCreateStatement(new CreateStatement());
        return operation;
    }

    public Operation generateCloseStatement() {
        final Operation operation = new Operation();
        operation.setCloseStatement(new CloseStatement());
        return operation;
    }

    public Operation generateExecuteStatement(final String sql) {
        final Operation operation = new Operation();

        final Execute.Statement statement = new Execute.Statement();
        statement.setSql(sql);

        final Execute execute = new Execute();
        execute.setStatement(statement);

        operation.setExecute(execute);

        return operation;
    }

    public Operation generateTransactionStarted() {
        final Operation operation = new Operation();
        operation.setTransactionStarted(new TransactionStarted());
        return operation;
    }

    public Operation generateTransactionCommitted() {
        final Operation operation = new Operation();
        operation.setTransactionCommitted(new TransactionCommitted());
        return operation;
    }

    public Operation generateTransactionRollback() {
        final Operation operation = new Operation();
        operation.setTransactionRollback(new TransactionRollback());
        return operation;
    }

    public Operation generateCreateCallableStatement(final String sql) {
        final Operation operation = new Operation();

        final CreateCallableStatement createCallableStatement = new CreateCallableStatement();
        createCallableStatement.setSql(sql);

        operation.setCreateCallableStatement(createCallableStatement);

        return operation;
    }
}
