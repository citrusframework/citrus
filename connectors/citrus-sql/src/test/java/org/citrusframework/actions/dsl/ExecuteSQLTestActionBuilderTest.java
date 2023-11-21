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

package org.citrusframework.actions.dsl;

import java.io.IOException;

import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.ExecuteSQLAction;
import org.citrusframework.spi.Resources;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.actions.ExecuteSQLAction.Builder.sql;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class ExecuteSQLTestActionBuilderTest extends UnitTestSupport {
    private final JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
    private final PlatformTransactionManager transactionManager = Mockito.mock(PlatformTransactionManager.class);

    @Test
    public void testExecuteSQLBuilderWithStatement() {
        reset(jdbcTemplate);

        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(sql().jdbcTemplate(jdbcTemplate)
            .statement("TEST_STMT_1")
            .statement("TEST_STMT_2")
            .statement("TEST_STMT_3")
            .ignoreErrors(false));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLAction.class);
        Assert.assertEquals(test.getActiveAction().getClass(), ExecuteSQLAction.class);

        ExecuteSQLAction action = (ExecuteSQLAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "sql");
        Assert.assertEquals(action.getStatements().toString(), "[TEST_STMT_1, TEST_STMT_2, TEST_STMT_3]");
        Assert.assertFalse(action.isIgnoreErrors());
        Assert.assertEquals(action.getJdbcTemplate(), jdbcTemplate);

        verify(jdbcTemplate).execute("TEST_STMT_1");
        verify(jdbcTemplate).execute("TEST_STMT_2");
        verify(jdbcTemplate).execute("TEST_STMT_3");
    }

    @Test
    public void testExecuteSQLBuilderWithTransaction() {
        reset(jdbcTemplate, transactionManager);

        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(sql().jdbcTemplate(jdbcTemplate)
                .transactionManager(transactionManager)
                .transactionTimeout(5000)
                .transactionIsolationLevel("ISOLATION_READ_COMMITTED")
                .statement("TEST_STMT_1")
                .statement("TEST_STMT_2")
                .statement("TEST_STMT_3")
                .ignoreErrors(false));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLAction.class);
        Assert.assertEquals(test.getActiveAction().getClass(), ExecuteSQLAction.class);

        ExecuteSQLAction action = (ExecuteSQLAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "sql");
        Assert.assertEquals(action.getStatements().toString(), "[TEST_STMT_1, TEST_STMT_2, TEST_STMT_3]");
        Assert.assertFalse(action.isIgnoreErrors());
        Assert.assertEquals(action.getJdbcTemplate(), jdbcTemplate);
        Assert.assertEquals(action.getTransactionManager(), transactionManager);
        Assert.assertEquals(action.getTransactionTimeout(), "5000");
        Assert.assertEquals(action.getTransactionIsolationLevel(), "ISOLATION_READ_COMMITTED");

        verify(jdbcTemplate).execute("TEST_STMT_1");
        verify(jdbcTemplate).execute("TEST_STMT_2");
        verify(jdbcTemplate).execute("TEST_STMT_3");
    }

    @Test
    public void testExecuteSQLBuilderWithResource() throws IOException {
        reset(jdbcTemplate);

        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(sql().jdbcTemplate(jdbcTemplate)
            .sqlResource(Resources.fromClasspath("org/citrusframework/actions/dsl/script.sql"))
            .ignoreErrors(true));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLAction.class);
        Assert.assertEquals(test.getActiveAction().getClass(), ExecuteSQLAction.class);

        ExecuteSQLAction action = (ExecuteSQLAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "sql");
        Assert.assertTrue(action.isIgnoreErrors());
        Assert.assertEquals(action.getJdbcTemplate(), jdbcTemplate);
        Assert.assertEquals(action.getStatements().size(), 3);
        Assert.assertNull(action.getSqlResourcePath());

        verify(jdbcTemplate).execute("TEST_STMT_1");
        verify(jdbcTemplate).execute("TEST_STMT_2");
        verify(jdbcTemplate).execute("TEST_STMT_3");
    }

    @Test
    public void testExecuteSQLBuilderWithResourcePath() throws IOException {
        reset(jdbcTemplate);
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(sql().jdbcTemplate(jdbcTemplate)
            .sqlResource("classpath:org/citrusframework/actions/dsl/script.sql"));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLAction.class);
        Assert.assertEquals(test.getActiveAction().getClass(), ExecuteSQLAction.class);

        ExecuteSQLAction action = (ExecuteSQLAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "sql");
        Assert.assertFalse(action.isIgnoreErrors());
        Assert.assertEquals(action.getJdbcTemplate(), jdbcTemplate);
        Assert.assertEquals(action.getSqlResourcePath(), "classpath:org/citrusframework/actions/dsl/script.sql");

        verify(jdbcTemplate).execute("TEST_STMT_1");
        verify(jdbcTemplate).execute("TEST_STMT_2");
        verify(jdbcTemplate).execute("TEST_STMT_3");
    }
}
