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

package org.citrusframework.sql.groovy;

import org.apache.commons.dbcp2.BasicDataSource;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.ExecuteSQLAction;
import org.citrusframework.actions.ExecuteSQLQueryAction;
import org.citrusframework.groovy.GroovyTestLoader;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.validation.script.sql.SqlResultSetScriptValidator;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class SqlTest extends AbstractGroovyActionDslTest {

    @BindToRegistry
    private final BasicDataSource dataSource = new BasicDataSource();

    @BindToRegistry
    private final PlatformTransactionManager mockTransactionManager = Mockito.mock(PlatformTransactionManager.class);

    @BindToRegistry
    private final SqlResultSetScriptValidator validator = Mockito.mock(SqlResultSetScriptValidator.class);

    @BeforeClass
    public void setupDataSource() {
        dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
        dataSource.setUrl("jdbc:hsqldb:mem:sql-groovy-test");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        new JdbcTemplate(dataSource).update("create table message (id integer, text varchar(50))");
    }

    @Test
    public void shouldLoadSql() {
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/sql/groovy/sql.test.groovy");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "SqlTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 2L);
        Assert.assertEquals(result.getTestAction(0).getClass(), ExecuteSQLAction.class);

        int actionIndex = 0;

        ExecuteSQLAction action = (ExecuteSQLAction) result.getTestAction(actionIndex++);
        Assert.assertNotNull(action.getDataSource());
        Assert.assertEquals(action.getDataSource(), dataSource);
        Assert.assertNull(action.getSqlResourcePath());
        Assert.assertEquals(action.getStatements().size(), 2);
        Assert.assertEquals(action.getStatements().get(0), "insert into message values (100, 'Hello from Citrus!')");
        Assert.assertEquals(action.getStatements().get(1), "update message set text='Hello' where id=100");
        Assert.assertFalse(action.isIgnoreErrors());
        Assert.assertNull(action.getTransactionManager());
        Assert.assertEquals(action.getTransactionTimeout(), "-1");
        Assert.assertEquals(action.getTransactionIsolationLevel(), "ISOLATION_DEFAULT");

        action = (ExecuteSQLAction) result.getTestAction(actionIndex);
        Assert.assertNotNull(action.getDataSource());
        Assert.assertEquals(action.getDataSource(), dataSource);
        Assert.assertNotNull(action.getSqlResourcePath());
        Assert.assertEquals(action.getSqlResourcePath(), "classpath:org/citrusframework/sql/test-statements.sql");
        Assert.assertEquals(action.getStatements().size(), 0);
        Assert.assertTrue(action.isIgnoreErrors());
        Assert.assertEquals(action.getTransactionManager(), mockTransactionManager);
        Assert.assertEquals(action.getTransactionTimeout(), "5000");
        Assert.assertEquals(action.getTransactionIsolationLevel(), "ISOLATION_READ_COMMITTED");
    }

    @Test
    public void shouldLoadSqlQuery() {
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/sql/groovy/sql-query.test.groovy");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "SqlQueryTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 6L);
        Assert.assertEquals(result.getTestAction(0).getClass(), ExecuteSQLAction.class);

        int actionIndex = 0;

        ExecuteSQLAction action = (ExecuteSQLAction) result.getTestAction(actionIndex++);
        Assert.assertNotNull(action.getDataSource());
        Assert.assertEquals(action.getDataSource(), dataSource);
        Assert.assertNull(action.getSqlResourcePath());
        Assert.assertEquals(action.getStatements().size(), 2);
        Assert.assertEquals(action.getStatements().get(0), "insert into message values (1000, 'Hello from Citrus!')");
        Assert.assertEquals(action.getStatements().get(1), "insert into message values (1001, 'Citrus rocks!')");
        Assert.assertFalse(action.isIgnoreErrors());
        Assert.assertNull(action.getTransactionManager());
        Assert.assertEquals(action.getTransactionTimeout(), "-1");
        Assert.assertEquals(action.getTransactionIsolationLevel(), "ISOLATION_DEFAULT");

        ExecuteSQLQueryAction queryAction = (ExecuteSQLQueryAction) result.getTestAction(actionIndex++);
        Assert.assertNotNull(queryAction.getDataSource());
        Assert.assertEquals(queryAction.getDataSource(), dataSource);
        Assert.assertNull(queryAction.getSqlResourcePath());
        Assert.assertEquals(queryAction.getStatements().size(), 1);
        Assert.assertEquals(queryAction.getStatements().get(0), "select text from message where id=1000");
        Assert.assertEquals(queryAction.getControlResultSet().size(), 1L);
        Assert.assertEquals(queryAction.getControlResultSet().get("text").size(), 1L);
        Assert.assertEquals(queryAction.getControlResultSet().get("text").get(0), "Hello from Citrus!");
        Assert.assertEquals(queryAction.getExtractVariables().size(), 1);
        Assert.assertEquals(queryAction.getExtractVariables().get("text"), "greeting");

        Assert.assertEquals(context.getVariable("greeting"), "Hello from Citrus!");

        queryAction = (ExecuteSQLQueryAction) result.getTestAction(actionIndex++);
        Assert.assertNotNull(queryAction.getDataSource());
        Assert.assertEquals(queryAction.getDataSource(), dataSource);
        Assert.assertNull(queryAction.getSqlResourcePath());
        Assert.assertEquals(queryAction.getStatements().size(), 1);
        Assert.assertEquals(queryAction.getStatements().get(0), "select text from message where id>=1000");
        Assert.assertEquals(queryAction.getControlResultSet().size(), 1L);
        Assert.assertEquals(queryAction.getControlResultSet().get("text").size(), 2L);
        Assert.assertEquals(queryAction.getControlResultSet().get("text").get(0), "Hello from Citrus!");
        Assert.assertEquals(queryAction.getControlResultSet().get("text").get(1), "Citrus rocks!");

        queryAction = (ExecuteSQLQueryAction) result.getTestAction(actionIndex++);
        Assert.assertNotNull(queryAction.getDataSource());
        Assert.assertEquals(queryAction.getDataSource(), dataSource);
        Assert.assertNull(queryAction.getSqlResourcePath());
        Assert.assertEquals(queryAction.getStatements().size(), 1);
        Assert.assertEquals(queryAction.getStatements().get(0), "select * from message where id>=1000");
        Assert.assertEquals(queryAction.getControlResultSet().size(), 2L);
        Assert.assertEquals(queryAction.getControlResultSet().get("id").size(), 2L);
        Assert.assertEquals(queryAction.getControlResultSet().get("id").get(0), "1000");
        Assert.assertEquals(queryAction.getControlResultSet().get("id").get(1), "1001");
        Assert.assertEquals(queryAction.getControlResultSet().get("text").size(), 2L);
        Assert.assertEquals(queryAction.getControlResultSet().get("text").get(0), "Hello from Citrus!");
        Assert.assertEquals(queryAction.getControlResultSet().get("text").get(1), "Citrus rocks!");

        queryAction = (ExecuteSQLQueryAction) result.getTestAction(actionIndex++);
        Assert.assertNotNull(queryAction.getDataSource());
        Assert.assertEquals(queryAction.getDataSource(), dataSource);
        Assert.assertNull(queryAction.getSqlResourcePath());
        Assert.assertEquals(queryAction.getStatements().size(), 1);
        Assert.assertEquals(queryAction.getStatements().get(0), "select * from message where id>=1000");
        Assert.assertEquals(queryAction.getControlResultSet().size(), 0L);
        Assert.assertNotNull(queryAction.getScriptValidationContext());
        Assert.assertEquals(queryAction.getScriptValidationContext().getValidationScript().trim(), "assert rows.size() == 2");

        queryAction = (ExecuteSQLQueryAction) result.getTestAction(actionIndex);
        Assert.assertNotNull(queryAction.getDataSource());
        Assert.assertEquals(queryAction.getDataSource(), dataSource);
        Assert.assertNull(queryAction.getSqlResourcePath());
        Assert.assertEquals(queryAction.getStatements().size(), 1);
        Assert.assertEquals(queryAction.getStatements().get(0), "select * from message where id>=1000");
        Assert.assertEquals(queryAction.getControlResultSet().size(), 0L);
        Assert.assertNotNull(queryAction.getScriptValidationContext());
        Assert.assertEquals(queryAction.getScriptValidationContext().getValidationScriptResourcePath(), "classpath:org/citrusframework/sql/validate.groovy");
    }

}
