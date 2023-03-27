/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.citrus.dsl.runner;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.actions.ExecutePLSQLAction;
import org.citrusframework.citrus.dsl.UnitTestSupport;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class ExecutePLSQLTestRunnerTest extends UnitTestSupport {
    private JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
    private PlatformTransactionManager transactionManager = Mockito.mock(PlatformTransactionManager.class);
    private Resource sqlResource = Mockito.mock(Resource.class);

    @Test
    public void testExecutePLSQLBuilderWithStatement() {
        reset(jdbcTemplate);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                plsql(builder -> builder.jdbcTemplate(jdbcTemplate)
                        .statement("TEST_STMT_1")
                        .statement("TEST_STMT_2")
                        .statement("TEST_STMT_3"));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecutePLSQLAction.class);
        Assert.assertEquals(test.getActiveAction().getClass(), ExecutePLSQLAction.class);

        ExecutePLSQLAction action = (ExecutePLSQLAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "plsql");
        Assert.assertFalse(action.isIgnoreErrors());
        Assert.assertEquals(action.getStatements().toString(), "[TEST_STMT_1, TEST_STMT_2, TEST_STMT_3]");
        Assert.assertNull(action.getScript());
        Assert.assertNull(action.getSqlResourcePath());
        Assert.assertEquals(action.getJdbcTemplate(), jdbcTemplate);

        verify(jdbcTemplate).execute("TEST_STMT_1");
        verify(jdbcTemplate).execute("TEST_STMT_2");
        verify(jdbcTemplate).execute("TEST_STMT_3");
    }

    @Test
    public void testExecutePLSQLBuilderWithTransaction() {
        reset(jdbcTemplate, transactionManager);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                plsql(builder -> builder.jdbcTemplate(jdbcTemplate)
                        .transactionManager(transactionManager)
                        .transactionTimeout(5000)
                        .transactionIsolationLevel("ISOLATION_READ_COMMITTED")
                        .statement("TEST_STMT_1")
                        .statement("TEST_STMT_2")
                        .statement("TEST_STMT_3"));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecutePLSQLAction.class);
        Assert.assertEquals(test.getActiveAction().getClass(), ExecutePLSQLAction.class);

        ExecutePLSQLAction action = (ExecutePLSQLAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "plsql");
        Assert.assertFalse(action.isIgnoreErrors());
        Assert.assertEquals(action.getStatements().toString(), "[TEST_STMT_1, TEST_STMT_2, TEST_STMT_3]");
        Assert.assertNull(action.getScript());
        Assert.assertNull(action.getSqlResourcePath());
        Assert.assertEquals(action.getJdbcTemplate(), jdbcTemplate);
        Assert.assertEquals(action.getTransactionManager(), transactionManager);
        Assert.assertEquals(action.getTransactionTimeout(), "5000");
        Assert.assertEquals(action.getTransactionIsolationLevel(), "ISOLATION_READ_COMMITTED");

        verify(jdbcTemplate).execute("TEST_STMT_1");
        verify(jdbcTemplate).execute("TEST_STMT_2");
        verify(jdbcTemplate).execute("TEST_STMT_3");
    }

    @Test
    public void testExecutePLSQLBuilderWithSQLResource() throws IOException {
        reset(jdbcTemplate, sqlResource);
        when(sqlResource.getInputStream()).thenReturn(new ByteArrayInputStream(("TEST_STMT_1\n" +
                "/\n" +
                "TEST_STMT_2\n" +
                "/\n" +
                "TEST_STMT_3\n" +
                "/").getBytes()));

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                plsql(builder -> builder.jdbcTemplate(jdbcTemplate)
                        .sqlResource(sqlResource));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecutePLSQLAction.class);
        Assert.assertEquals(test.getActiveAction().getClass(), ExecutePLSQLAction.class);

        ExecutePLSQLAction action = (ExecutePLSQLAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "plsql");
        Assert.assertFalse(action.isIgnoreErrors());
        Assert.assertEquals(action.getStatements().size(), 0L);
        Assert.assertEquals(action.getScript(), ("TEST_STMT_1\n" +
                "/\n" +
                "TEST_STMT_2\n" +
                "/\n" +
                "TEST_STMT_3\n" +
                "/"));
        Assert.assertEquals(action.getJdbcTemplate(), jdbcTemplate);

        verify(jdbcTemplate).execute("TEST_STMT_1");
        verify(jdbcTemplate).execute("TEST_STMT_2");
        verify(jdbcTemplate).execute("TEST_STMT_3");
    }

    @Test
    public void testExecutePLSQLBuilderWithSQLResourcePath() throws IOException {
        reset(jdbcTemplate);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                plsql(builder -> builder.jdbcTemplate(jdbcTemplate)
                        .sqlResource("classpath:org/citrusframework/citrus/dsl/runner/plsql.sql"));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecutePLSQLAction.class);
        Assert.assertEquals(test.getActiveAction().getClass(), ExecutePLSQLAction.class);

        ExecutePLSQLAction action = (ExecutePLSQLAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "plsql");
        Assert.assertFalse(action.isIgnoreErrors());
        Assert.assertEquals(action.getStatements().size(), 0L);
        Assert.assertNull(action.getScript());
        Assert.assertEquals(action.getSqlResourcePath(), "classpath:org/citrusframework/citrus/dsl/runner/plsql.sql");
        Assert.assertEquals(action.getJdbcTemplate(), jdbcTemplate);

        verify(jdbcTemplate).execute("TEST_STMT_1");
        verify(jdbcTemplate).execute("TEST_STMT_2");
        verify(jdbcTemplate).execute("TEST_STMT_3");
    }

    @Test
    public void testExecutePLSQLBuilderWithInlineScript() {
        reset(jdbcTemplate);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                plsql(builder -> builder.jdbcTemplate(jdbcTemplate)
                        .ignoreErrors(true)
                        .sqlScript(("TEST_STMT_1\n" +
                                "/\n" +
                                "TEST_STMT_2\n" +
                                "/")));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecutePLSQLAction.class);
        Assert.assertEquals(test.getActiveAction().getClass(), ExecutePLSQLAction.class);

        ExecutePLSQLAction action = (ExecutePLSQLAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "plsql");
        Assert.assertTrue(action.isIgnoreErrors());
        Assert.assertEquals(action.getStatements().size(), 0L);
        Assert.assertNull(action.getSqlResourcePath());
        Assert.assertEquals(action.getScript(), ("TEST_STMT_1\n" +
                "/\n" +
                "TEST_STMT_2\n" +
                "/"));
        Assert.assertEquals(action.getJdbcTemplate(), jdbcTemplate);

        verify(jdbcTemplate).execute("TEST_STMT_1");
        verify(jdbcTemplate).execute("TEST_STMT_2");
    }
}
