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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

public class ExecuteSQLActionTest extends AbstractTestNGUnitTest {

    private static final String DB_STMT_1 = "DELETE * FROM ERRORS WHERE STATUS='resolved'";
    private static final String DB_STMT_2 = "DELETE * FROM CONFIGURATION WHERE VERSION=1";
    private static final String DB_STMT_3 = "DELETE * FROM ORDERS WHERE STATE='cancelled'";
    private static final String DB_STMT_4 = "DELETE * FROM CUSTOMERS WHERE ACTIVE=false";

    private ExecuteSQLAction.Builder executeSQLActionBuilder;

    private JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
    private PlatformTransactionManager transactionManager = Mockito.mock(PlatformTransactionManager.class);

    @BeforeMethod
    public void setUp() {
        executeSQLActionBuilder = new ExecuteSQLAction.Builder()
                .jdbcTemplate(jdbcTemplate);
    }

    @Test
    public void testSQLExecutionWithInlineStatements() {
        List<String> stmts = new ArrayList<>();
        stmts.add(DB_STMT_1);
        stmts.add(DB_STMT_2);

        executeSQLActionBuilder.statements(stmts);

        reset(jdbcTemplate);

        executeSQLActionBuilder.build().execute(context);

        verify(jdbcTemplate).execute(DB_STMT_1);
        verify(jdbcTemplate).execute(DB_STMT_2);
    }

    @Test
    public void testSQLExecutionWithTransactions() {
        List<String> stmts = new ArrayList<>();
        stmts.add(DB_STMT_1);
        stmts.add(DB_STMT_2);

        executeSQLActionBuilder.statements(stmts);
        executeSQLActionBuilder.transactionManager(transactionManager);

        reset(jdbcTemplate, transactionManager);

        executeSQLActionBuilder.build().execute(context);

        verify(jdbcTemplate).execute(DB_STMT_1);
        verify(jdbcTemplate).execute(DB_STMT_2);
    }

    @Test
    public void testSQLExecutionWithFileResource() {
        executeSQLActionBuilder.sqlResource("classpath:org/citrusframework/sql/actions/test-sql-statements.sql");

        reset(jdbcTemplate);

        executeSQLActionBuilder.build().execute(context);

        verify(jdbcTemplate).execute(DB_STMT_1);
        verify(jdbcTemplate).execute(DB_STMT_2);
    }

    @Test
    public void testCustomResourceParsingHookIsUsed() {
        executeSQLActionBuilder.sqlResource("classpath:org/citrusframework/sql/actions/test-sql-statements.sql");

        ExecuteSQLAction action = Mockito.spy(executeSQLActionBuilder.build());

        doReturn(List.of("DELETE * FROM CUSTOM_PARSED")).when(action)
                .createStatementsFromFileResource(any(Resource.class), any());

        reset(jdbcTemplate);

        action.execute(context);

        verify(jdbcTemplate).execute("DELETE * FROM CUSTOM_PARSED");
    }

    @Test
    public void testSQLExecutionWithBlankResourcePathIsIgnored() {
        executeSQLActionBuilder.sqlResource((String) null);
        executeSQLActionBuilder.sqlResource("   ");
        executeSQLActionBuilder.statement(DB_STMT_1);

        ExecuteSQLAction action = executeSQLActionBuilder.build();

        Assert.assertTrue(action.getSqlResourcePaths().isEmpty());
        Assert.assertEquals(action.getSqlStatementSources().size(), 1);

        reset(jdbcTemplate);

        action.execute(context);

        verify(jdbcTemplate).execute(DB_STMT_1);
    }

    @Test
    public void testSQLExecutionWithFileSystemResource() throws Exception {
        String absolutePath = absolutePathOf("org/citrusframework/sql/actions/test-sql-statements.sql");

        executeSQLActionBuilder.sqlResource("file:" + absolutePath);

        reset(jdbcTemplate);

        executeSQLActionBuilder.build().execute(context);

        verify(jdbcTemplate).execute(DB_STMT_1);
        verify(jdbcTemplate).execute(DB_STMT_2);
    }

    @Test
    public void testSQLExecutionWithAbsoluteFilePath() throws Exception {
        String absolutePath = absolutePathOf("org/citrusframework/sql/actions/test-sql-statements.sql");

        executeSQLActionBuilder.sqlResource(absolutePath);

        reset(jdbcTemplate);

        executeSQLActionBuilder.build().execute(context);

        verify(jdbcTemplate).execute(DB_STMT_1);
        verify(jdbcTemplate).execute(DB_STMT_2);
    }

    @Test
    public void testSQLExecutionWithMissingFileSystemResource() {
        executeSQLActionBuilder.sqlResource("file:/nonexistent/path/to/sql-script.sql");

        CitrusRuntimeException exception = Assert.expectThrows(CitrusRuntimeException.class,
                () -> executeSQLActionBuilder.build().execute(context));

        Assert.assertTrue(exception.getMessage().contains("sql-script.sql"),
                "Exception message should name the missing resource but was: " + exception.getMessage());
    }

    @Test
    public void testSQLExecutionWithMissingClasspathResource() {
        executeSQLActionBuilder.sqlResource("classpath:org/citrusframework/sql/actions/nonexistent-sql-script.sql");

        CitrusRuntimeException exception = Assert.expectThrows(CitrusRuntimeException.class,
                () -> executeSQLActionBuilder.build().execute(context));

        Assert.assertTrue(exception.getMessage().contains("nonexistent-sql-script.sql"),
                "Exception message should name the missing resource but was: " + exception.getMessage());
    }

    private static String absolutePathOf(String classpathLocation) throws Exception {
        URL resource = ExecuteSQLActionTest.class.getClassLoader().getResource(classpathLocation);
        Assert.assertNotNull(resource, "Test resource not found on classpath: " + classpathLocation);
        return new File(resource.toURI()).getAbsolutePath();
    }

    @Test
    public void testSQLExecutionWithChainedFileResources() {
        executeSQLActionBuilder.sqlResource("classpath:org/citrusframework/sql/actions/test-sql-statements.sql");
        executeSQLActionBuilder.sqlResource("classpath:org/citrusframework/sql/actions/test-sql-statements-2.sql");

        ExecuteSQLAction action = executeSQLActionBuilder.build();
        Assert.assertEquals(action.getSqlResourcePaths(),
                List.of("classpath:org/citrusframework/sql/actions/test-sql-statements.sql",
                        "classpath:org/citrusframework/sql/actions/test-sql-statements-2.sql"));
        Assert.assertEquals(action.getSqlResourcePath(), "classpath:org/citrusframework/sql/actions/test-sql-statements.sql");

        reset(jdbcTemplate);

        action.execute(context);

        InOrder inOrder = Mockito.inOrder(jdbcTemplate);
        inOrder.verify(jdbcTemplate).execute(DB_STMT_1);
        inOrder.verify(jdbcTemplate).execute(DB_STMT_2);
        inOrder.verify(jdbcTemplate).execute(DB_STMT_3);
        inOrder.verify(jdbcTemplate).execute(DB_STMT_4);
    }

    @Test
    public void testSQLExecutionWithMixedResourceAndFilePath() {
        executeSQLActionBuilder.sqlResource(Resources.fromClasspath("org/citrusframework/sql/actions/test-sql-statements-2.sql"));
        executeSQLActionBuilder.sqlResource("classpath:org/citrusframework/sql/actions/test-sql-statements.sql");

        reset(jdbcTemplate);

        executeSQLActionBuilder.build().execute(context);

        InOrder inOrder = Mockito.inOrder(jdbcTemplate);
        inOrder.verify(jdbcTemplate).execute(DB_STMT_3);
        inOrder.verify(jdbcTemplate).execute(DB_STMT_4);
        inOrder.verify(jdbcTemplate).execute(DB_STMT_1);
        inOrder.verify(jdbcTemplate).execute(DB_STMT_2);
    }

    @Test
    public void testSQLExecutionWithInlineStatementsAndFileResource() {
        executeSQLActionBuilder.statement(DB_STMT_3);
        executeSQLActionBuilder.sqlResource("classpath:org/citrusframework/sql/actions/test-sql-statements.sql");

        reset(jdbcTemplate);

        executeSQLActionBuilder.build().execute(context);

        InOrder inOrder = Mockito.inOrder(jdbcTemplate);
        inOrder.verify(jdbcTemplate).execute(DB_STMT_3);
        inOrder.verify(jdbcTemplate).execute(DB_STMT_1);
        inOrder.verify(jdbcTemplate).execute(DB_STMT_2);
    }

    @Test
    public void testSQLExecutionWithInlineScriptVariableSupport() {
        context.setVariable("resolvedStatus", "resolved");
        context.setVariable("version", "1");

        List<String> stmts = new ArrayList<>();
        stmts.add("DELETE * FROM ERRORS WHERE STATUS='${resolvedStatus}'");
        stmts.add("DELETE * FROM CONFIGURATION WHERE VERSION=${version}");

        executeSQLActionBuilder.statements(stmts);

        reset(jdbcTemplate);

        executeSQLActionBuilder.build().execute(context);

        verify(jdbcTemplate).execute(DB_STMT_1);
        verify(jdbcTemplate).execute(DB_STMT_2);
    }

    @Test
    public void testSQLExecutionWithFileResourceVariableSupport() {
        context.setVariable("resolvedStatus", "resolved");
        context.setVariable("version", "1");

        executeSQLActionBuilder.sqlResource("classpath:org/citrusframework/sql/actions/test-sql-with-variables.sql");

        reset(jdbcTemplate);

        executeSQLActionBuilder.build().execute(context);

        verify(jdbcTemplate).execute(DB_STMT_1);
        verify(jdbcTemplate).execute(DB_STMT_2);
    }

    @Test
    public void testSQLExecutionIgnoreErrors() {
        List<String> stmts = new ArrayList<>();
        stmts.add(DB_STMT_1);
        stmts.add(DB_STMT_2);

        executeSQLActionBuilder.statements(stmts);
        executeSQLActionBuilder.ignoreErrors(true);

        reset(jdbcTemplate);

        doThrow(new DataAccessException("Something went wrong!") {
        }).when(jdbcTemplate).execute(DB_STMT_2);

        executeSQLActionBuilder.build().execute(context);
        verify(jdbcTemplate).execute(DB_STMT_1);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testSQLExecutionErrorForwarding() {
        List<String> stmts = new ArrayList<>();
        stmts.add(DB_STMT_1);
        stmts.add(DB_STMT_2);

        executeSQLActionBuilder.statements(stmts);
        executeSQLActionBuilder.ignoreErrors(false);

        reset(jdbcTemplate);

        doThrow(new DataAccessException("Something went wrong!") {
        }).when(jdbcTemplate).execute(DB_STMT_2);

        executeSQLActionBuilder.build().execute(context);
        verify(jdbcTemplate).execute(DB_STMT_1);
    }

    @Test
    public void testNoJdbcTemplateConfigured() {
        // Special ExecuteSQLQueryAction without a JdbcTemplate
        executeSQLActionBuilder = new ExecuteSQLAction.Builder().jdbcTemplate(null);
        executeSQLActionBuilder.statements(Collections.singletonList("statement"));

        CitrusRuntimeException exception = Assert.expectThrows(CitrusRuntimeException.class, () -> executeSQLActionBuilder.build().execute(context));

        Assert.assertEquals(exception.getMessage(), "No JdbcTemplate configured for sql execution!");
    }
}
