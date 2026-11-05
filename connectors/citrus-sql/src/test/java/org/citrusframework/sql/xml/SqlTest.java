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

package org.citrusframework.sql.xml;

import java.util.List;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.sql.actions.ExecuteSQLAction;
import org.citrusframework.sql.actions.ExecuteSQLQueryAction;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.validation.context.script.DefaultScriptValidationContext;
import org.citrusframework.validation.script.sql.SqlResultSetScriptValidator;
import org.citrusframework.xml.XmlTestLoader;
import org.citrusframework.xml.actions.XmlTestActionBuilder;
import org.apache.commons.dbcp2.BasicDataSource;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SqlTest extends AbstractXmlActionTest {

    @BindToRegistry
    private final BasicDataSource dataSource = new BasicDataSource();

    @BindToRegistry
    private final PlatformTransactionManager mockTransactionManager = Mockito.mock(PlatformTransactionManager.class);

    @BindToRegistry
    private final SqlResultSetScriptValidator validator = Mockito.mock(SqlResultSetScriptValidator.class);

    @BeforeAll
    public void setupDataSource() {
        dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
        dataSource.setUrl("jdbc:hsqldb:mem:sql-xml-test");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        new JdbcTemplate(dataSource).update("create table message (id integer, text varchar(50))");
    }

    @Test
    public void shouldLoadSql() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/sql/xml/sql.citrus.it.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("SqlTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(2L, result.getActionCount());
        Assertions.assertEquals(ExecuteSQLAction.class, result.getTestAction(0).getClass());

        int actionIndex = 0;

        ExecuteSQLAction action = (ExecuteSQLAction) result.getTestAction(actionIndex++);
        Assertions.assertNotNull(action.getDataSource());
        Assertions.assertEquals(dataSource, action.getDataSource());
        Assertions.assertNull(action.getSqlResourcePath());
        Assertions.assertEquals(2, action.getStatements().size());
        Assertions.assertEquals("insert into message values (100, 'Hello from Citrus!')", action.getStatements().get(0));
        Assertions.assertEquals("update message set text='Hello' where id=100", action.getStatements().get(1));
        Assertions.assertFalse(action.isIgnoreErrors());
        Assertions.assertNull(action.getTransactionManager());
        Assertions.assertEquals("-1", action.getTransactionTimeout());
        Assertions.assertEquals("ISOLATION_DEFAULT", action.getTransactionIsolationLevel());

        action = (ExecuteSQLAction) result.getTestAction(actionIndex);
        Assertions.assertNotNull(action.getDataSource());
        Assertions.assertEquals(dataSource, action.getDataSource());
        Assertions.assertNotNull(action.getSqlResourcePath());
        Assertions.assertEquals("classpath:org/citrusframework/sql/test-statements.sql", action.getSqlResourcePath());
        Assertions.assertEquals(0, action.getStatements().size());
        Assertions.assertTrue(action.isIgnoreErrors());
        Assertions.assertEquals(mockTransactionManager, action.getTransactionManager());
        Assertions.assertEquals("5000", action.getTransactionTimeout());
        Assertions.assertEquals("ISOLATION_READ_COMMITTED", action.getTransactionIsolationLevel());
    }

    @Test
    public void shouldLoadSqlWithFileAndInlineStatement() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/sql/xml/sql-file-and-inline.citrus.it.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals(1L, result.getActionCount());

        ExecuteSQLAction action = (ExecuteSQLAction) result.getTestAction(0);
        Assertions.assertEquals(1, action.getStatements().size());
        Assertions.assertEquals("insert into message values (302, 'Inline')", action.getStatements().get(0));
        Assertions.assertEquals(List.of("classpath:org/citrusframework/sql/test-statements-a.sql"), action.getSqlResourcePaths());

        Integer count = new JdbcTemplate(dataSource).queryForObject("select count(*) from message where id in (300, 302)", Integer.class);
        Assertions.assertEquals(Integer.valueOf(2), count);
    }

    @Test
    public void shouldLoadSqlQuery() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/sql/xml/sql-query.citrus.it.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("SqlQueryTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(6L, result.getActionCount());
        Assertions.assertEquals(ExecuteSQLAction.class, result.getTestAction(0).getClass());

        int actionIndex = 0;

        ExecuteSQLAction action = (ExecuteSQLAction) result.getTestAction(actionIndex++);
        Assertions.assertNotNull(action.getDataSource());
        Assertions.assertEquals(dataSource, action.getDataSource());
        Assertions.assertNull(action.getSqlResourcePath());
        Assertions.assertEquals(2, action.getStatements().size());
        Assertions.assertEquals("insert into message values (1000, 'Hello from Citrus!')", action.getStatements().get(0));
        Assertions.assertEquals("insert into message values (1001, 'Citrus rocks!')", action.getStatements().get(1));
        Assertions.assertFalse(action.isIgnoreErrors());
        Assertions.assertNull(action.getTransactionManager());
        Assertions.assertEquals("-1", action.getTransactionTimeout());
        Assertions.assertEquals("ISOLATION_DEFAULT", action.getTransactionIsolationLevel());

        ExecuteSQLQueryAction queryAction = (ExecuteSQLQueryAction) result.getTestAction(actionIndex++);
        Assertions.assertNotNull(queryAction.getDataSource());
        Assertions.assertEquals(dataSource, queryAction.getDataSource());
        Assertions.assertNull(queryAction.getSqlResourcePath());
        Assertions.assertEquals(1, queryAction.getStatements().size());
        Assertions.assertEquals("select text from message where id=1000", queryAction.getStatements().get(0));
        Assertions.assertEquals(1L, queryAction.getControlResultSet().size());
        Assertions.assertEquals(1L, queryAction.getControlResultSet().get("text").size());
        Assertions.assertEquals("Hello from Citrus!", queryAction.getControlResultSet().get("text").get(0));
        Assertions.assertEquals(1, queryAction.getExtractVariables().size());
        Assertions.assertEquals("greeting", queryAction.getExtractVariables().get("text"));

        Assertions.assertEquals("Hello from Citrus!", context.getVariable("greeting"));

        queryAction = (ExecuteSQLQueryAction) result.getTestAction(actionIndex++);
        Assertions.assertNotNull(queryAction.getDataSource());
        Assertions.assertEquals(dataSource, queryAction.getDataSource());
        Assertions.assertNull(queryAction.getSqlResourcePath());
        Assertions.assertEquals(1, queryAction.getStatements().size());
        Assertions.assertEquals("select text from message where id>=1000", queryAction.getStatements().get(0));
        Assertions.assertEquals(1L, queryAction.getControlResultSet().size());
        Assertions.assertEquals(2L, queryAction.getControlResultSet().get("text").size());
        Assertions.assertEquals("Hello from Citrus!", queryAction.getControlResultSet().get("text").get(0));
        Assertions.assertEquals("Citrus rocks!", queryAction.getControlResultSet().get("text").get(1));

        queryAction = (ExecuteSQLQueryAction) result.getTestAction(actionIndex++);
        Assertions.assertNotNull(queryAction.getDataSource());
        Assertions.assertEquals(dataSource, queryAction.getDataSource());
        Assertions.assertNull(queryAction.getSqlResourcePath());
        Assertions.assertEquals(1, queryAction.getStatements().size());
        Assertions.assertEquals("select * from message where id>=1000", queryAction.getStatements().get(0));
        Assertions.assertEquals(2L, queryAction.getControlResultSet().size());
        Assertions.assertEquals(2L, queryAction.getControlResultSet().get("id").size());
        Assertions.assertEquals("1000", queryAction.getControlResultSet().get("id").get(0));
        Assertions.assertEquals("1001", queryAction.getControlResultSet().get("id").get(1));
        Assertions.assertEquals(2L, queryAction.getControlResultSet().get("text").size());
        Assertions.assertEquals("Hello from Citrus!", queryAction.getControlResultSet().get("text").get(0));
        Assertions.assertEquals("Citrus rocks!", queryAction.getControlResultSet().get("text").get(1));

        queryAction = (ExecuteSQLQueryAction) result.getTestAction(actionIndex++);
        Assertions.assertNotNull(queryAction.getDataSource());
        Assertions.assertEquals(dataSource, queryAction.getDataSource());
        Assertions.assertNull(queryAction.getSqlResourcePath());
        Assertions.assertEquals(1, queryAction.getStatements().size());
        Assertions.assertEquals("select * from message where id>=1000", queryAction.getStatements().get(0));
        Assertions.assertEquals(0L, queryAction.getControlResultSet().size());
        Assertions.assertNotNull(queryAction.getScriptValidationContext());
        Assertions.assertEquals("assert rows.size() == 2", ((DefaultScriptValidationContext) queryAction.getScriptValidationContext()).getValidationScript().trim());

        queryAction = (ExecuteSQLQueryAction) result.getTestAction(actionIndex);
        Assertions.assertNotNull(queryAction.getDataSource());
        Assertions.assertEquals(dataSource, queryAction.getDataSource());
        Assertions.assertNull(queryAction.getSqlResourcePath());
        Assertions.assertEquals(1, queryAction.getStatements().size());
        Assertions.assertEquals("select * from message where id>=1000", queryAction.getStatements().get(0));
        Assertions.assertEquals(0L, queryAction.getControlResultSet().size());
        Assertions.assertNotNull(queryAction.getScriptValidationContext());
        Assertions.assertEquals("classpath:org/citrusframework/sql/validate.groovy", ((DefaultScriptValidationContext) queryAction.getScriptValidationContext()).getValidationScriptResourcePath());
    }

    @Test
    public void shouldLookupTestActionBuilder() {
        Assertions.assertTrue(XmlTestActionBuilder.lookup("sql").isPresent());
        Assertions.assertEquals(Sql.class, XmlTestActionBuilder.lookup("sql").get().getClass());

        Assertions.assertTrue(XmlTestActionBuilder.lookup("plsql").isPresent());
        Assertions.assertEquals(Plsql.class, XmlTestActionBuilder.lookup("plsql").get().getClass());
    }
}
