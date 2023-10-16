/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.actions;

import org.apache.commons.codec.binary.Base64;
import org.citrusframework.CitrusSettings;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.script.ScriptTypes;
import org.citrusframework.validation.script.ScriptValidationContext;
import org.citrusframework.validation.script.sql.SqlResultSetScriptValidator;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class ExecuteSQLQueryActionTest extends UnitTestSupport {

    private static final String DB_STMT_1 = "select ORDERTYPE, STATUS from orders where ID = 5";
    private static final String DB_STMT_2 = "select NAME, HEIGHT from customers where ID = 1";
    private static final String DB_STMT_3 = "WITH RECURSIVE relations AS (SELECT id, framework_name FROM framework f INNER JOIN relations r ON f.relation_id = r.id) SELECT * FROM relations";

    private ExecuteSQLQueryAction.Builder executeSQLQueryAction;

    private JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
    private PlatformTransactionManager transactionManager = Mockito.mock(PlatformTransactionManager.class);
    private SqlResultSetScriptValidator resultSetScriptValidator = Mockito.mock(SqlResultSetScriptValidator.class);

    @Override
    protected TestContextFactory createTestContextFactory() {
        TestContextFactory factory = super.createTestContextFactory();
        factory.getReferenceResolver().bind("sqlResultSetScriptValidator", resultSetScriptValidator);
        return factory;
    }

    @BeforeMethod
    public void setUp() {
        executeSQLQueryAction = new ExecuteSQLQueryAction.Builder()
                .jdbcTemplate(jdbcTemplate);
    }

    @Test
    public void testSQLStatement() {
        String sql = DB_STMT_1;
        reset(jdbcTemplate);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("ORDERTYPE", "small");
        resultMap.put("STATUS", "in_progress");

        when(jdbcTemplate.queryForList(sql)).thenReturn(Collections.singletonList(resultMap));

        executeSQLQueryAction.statements(Collections.singletonList(sql));
        executeSQLQueryAction.extract("ORDERTYPE", "orderType");
        executeSQLQueryAction.extract("STATUS", "status");
        executeSQLQueryAction.build().execute(context);

        Assert.assertNotNull(context.getVariable("${orderType}"));
        Assert.assertEquals(context.getVariable("${orderType}"), "small");
        Assert.assertNotNull(context.getVariable("${status}"));
        Assert.assertEquals(context.getVariable("${status}"), "in_progress");
    }

    @Test
    public void testSQLStatementWithTransaction() {
        reset(jdbcTemplate, transactionManager);
        TransactionStatus transactionStatusMock = mock(TransactionStatus.class);
        doReturn(transactionStatusMock).when(transactionManager).getTransaction(any(TransactionTemplate.class));

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("ORDERTYPE", "small");
        resultMap.put("STATUS", "in_progress");

        when(jdbcTemplate.queryForList(DB_STMT_1)).thenReturn(Collections.singletonList(resultMap));

        executeSQLQueryAction.statements(Collections.singletonList(DB_STMT_1));
        executeSQLQueryAction.transactionManager(transactionManager);
        executeSQLQueryAction.extract("ORDERTYPE", "orderType");
        executeSQLQueryAction.extract("STATUS", "status");
        executeSQLQueryAction.build().execute(context);

        verify(transactionManager).commit(transactionStatusMock);

        Assert.assertNotNull(context.getVariable("${orderType}"));
        Assert.assertEquals(context.getVariable("${orderType}"), "small");
        Assert.assertNotNull(context.getVariable("${status}"));
        Assert.assertEquals(context.getVariable("${status}"), "in_progress");
    }

    @Test
    public void testSQLStatementLowerCaseColumnNames() {
        String sql = DB_STMT_1;
        reset(jdbcTemplate);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("ordertype", "small");
        resultMap.put("status", "in_progress");

        when(jdbcTemplate.queryForList(sql)).thenReturn(Collections.singletonList(resultMap));

        executeSQLQueryAction.statements(Collections.singletonList(sql));
        executeSQLQueryAction.extract("ORDERTYPE", "orderType");
        executeSQLQueryAction.extract("STATUS", "status");
        executeSQLQueryAction.build().execute(context);

        Assert.assertNotNull(context.getVariable("${orderType}"));
        Assert.assertEquals(context.getVariable("${orderType}"), "small");
        Assert.assertNotNull(context.getVariable("${status}"));
        Assert.assertEquals(context.getVariable("${status}"), "in_progress");
    }

    @Test
    public void testSQLMultipleStatements() {
        String sql1 = DB_STMT_1;
        String sql2 = DB_STMT_2;
        String sql3 = DB_STMT_3;
        reset(jdbcTemplate);

        Map<String, Object> resultMap1 = new HashMap<>();
        resultMap1.put("ORDERTYPE", "small");
        resultMap1.put("STATUS", "in_progress");

        when(jdbcTemplate.queryForList(sql1)).thenReturn(Collections.singletonList(resultMap1));

        Map<String, Object> resultMap2 = new HashMap<>();
        resultMap2.put("NAME", "Mickey Mouse");
        resultMap2.put("HEIGHT", "0,3");

        when(jdbcTemplate.queryForList(sql2)).thenReturn(Collections.singletonList(resultMap2));

        Map<String, Object> resultMap3 = new HashMap<>();
        resultMap2.put("ID", "1234");
        resultMap2.put("FRAMEWORK_NAME", "citrusframework/citrus");

        when(jdbcTemplate.queryForList(sql3)).thenReturn(Collections.singletonList(resultMap3));

        List<String> statements = new ArrayList<>();
        statements.add(sql1);
        statements.add(sql2);
        statements.add(sql3);

        executeSQLQueryAction.statements(statements);
        executeSQLQueryAction.extract("ORDERTYPE", "orderType");
        executeSQLQueryAction.extract("STATUS", "status");
        executeSQLQueryAction.extract("NAME", "name");
        executeSQLQueryAction.extract("HEIGHT", "height");
        executeSQLQueryAction.extract("ID", "id");
        executeSQLQueryAction.extract("FRAMEWORK_NAME", "frameworkName");
        executeSQLQueryAction.build().execute(context);

        Assert.assertNotNull(context.getVariable("${orderType}"));
        Assert.assertEquals(context.getVariable("${orderType}"), "small");
        Assert.assertNotNull(context.getVariable("${status}"));
        Assert.assertEquals(context.getVariable("${status}"), "in_progress");
        Assert.assertNotNull(context.getVariable("${name}"));
        Assert.assertEquals(context.getVariable("${name}"), "Mickey Mouse");
        Assert.assertNotNull(context.getVariable("${height}"));
        Assert.assertEquals(context.getVariable("${height}"), "0,3");
        Assert.assertNotNull(context.getVariable("${id}"));
        Assert.assertEquals(context.getVariable("${id}"), "1234");
        Assert.assertNotNull(context.getVariable("${frameworkName}"));
        Assert.assertEquals(context.getVariable("${frameworkName}"), "citrusframework/citrus");
    }

    @Test
    public void testNullValue() {
        String sql = DB_STMT_1;
        reset(jdbcTemplate);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("ORDERTYPE", "small");
        resultMap.put("STATUS", null);

        when(jdbcTemplate.queryForList(sql)).thenReturn(Collections.singletonList(resultMap));

        executeSQLQueryAction.statements(Collections.singletonList(sql));
        executeSQLQueryAction.build().execute(context);
    }

    @Test
    public void testVariableSupport() {
        context.setVariable("orderId", "5");

        String sql = "select ORDERTYPE, STATUS from orders where ID = ${orderId}";
        reset(jdbcTemplate);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("ORDERTYPE", "small");
        resultMap.put("STATUS", "in_progress");

        when(jdbcTemplate.queryForList(DB_STMT_1)).thenReturn(Collections.singletonList(resultMap));

        executeSQLQueryAction.statements(Collections.singletonList(sql));
        executeSQLQueryAction.build().execute(context);
    }

    @Test
    public void testExtractToVariablesLowerCaseColumnNames() {
        String sql = DB_STMT_1;
        reset(jdbcTemplate);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("ordertype", "small");
        resultMap.put("status", "in_progress");

        when(jdbcTemplate.queryForList(sql)).thenReturn(Collections.singletonList(resultMap));

        executeSQLQueryAction.statements(Collections.singletonList(sql));
        executeSQLQueryAction.extract("ordertype", "orderType");
        executeSQLQueryAction.extract("STATUS", "orderStatus");
        executeSQLQueryAction.build().execute(context);

        Assert.assertNotNull(context.getVariable("${orderStatus}"));
        Assert.assertEquals(context.getVariable("${orderStatus}"), "in_progress");
        Assert.assertNotNull(context.getVariable("${orderType}"));
        Assert.assertEquals(context.getVariable("${orderType}"), "small");
    }

    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testExtractToVariablesUnknownColumnMapping() {
        String sql = DB_STMT_1;
        reset(jdbcTemplate);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("ORDERTYPE", "small");
        resultMap.put("STATUS", "in_progress");

        when(jdbcTemplate.queryForList(sql)).thenReturn(Collections.singletonList(resultMap));

        executeSQLQueryAction.statements(Collections.singletonList(sql));
        executeSQLQueryAction.extract("UNKNOWN_COLUMN", "orderStatus");
        executeSQLQueryAction.build().execute(context);
    }

    @Test
    public void testResultSetValidation() {
        String sql = DB_STMT_1;
        reset(jdbcTemplate);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("ORDERTYPE", "small");
        resultMap.put("STATUS", "in_progress");

        when(jdbcTemplate.queryForList(sql)).thenReturn(Collections.singletonList(resultMap));

        executeSQLQueryAction.statements(Collections.singletonList(sql));
        executeSQLQueryAction.validate("ORDERTYPE", "small");
        executeSQLQueryAction.validate("STATUS", "in_progress");
        executeSQLQueryAction.build().execute(context);
    }

    @Test
    public void testResultSetValidationLowerCase() {
        String sql = DB_STMT_1;
        reset(jdbcTemplate);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("ordertype", "small");
        resultMap.put("status", "in_progress");

        when(jdbcTemplate.queryForList(sql)).thenReturn(Collections.singletonList(resultMap));

        executeSQLQueryAction.statements(Collections.singletonList(sql));
        executeSQLQueryAction.validate("ORDERTYPE", "small");
        executeSQLQueryAction.validate("STATUS", "in_progress");
        executeSQLQueryAction.build().execute(context);
    }

    @Test
    public void testResultSetValidationWithAliasNames() {
        String sql = "select ORDERTYPE AS TYPE, STATUS AS STATE from orders where ID=5";
        reset(jdbcTemplate);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("TYPE", "small");
        resultMap.put("STATE", "in_progress");

        when(jdbcTemplate.queryForList(sql)).thenReturn(Collections.singletonList(resultMap));

        executeSQLQueryAction.statements(Collections.singletonList(sql));
        executeSQLQueryAction.validate("TYPE", "small");
        executeSQLQueryAction.validate("STATE", "in_progress");
        executeSQLQueryAction.build().execute(context);
    }

    @Test
    public void testResultSetValidationError() {
        String sql = DB_STMT_1;
        reset(jdbcTemplate);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("ORDERTYPE", "small");
        resultMap.put("STATUS", "in_progress");

        when(jdbcTemplate.queryForList(sql)).thenReturn(Collections.singletonList(resultMap));

        executeSQLQueryAction.statements(Collections.singletonList(sql));
        executeSQLQueryAction.validate("ORDERTYPE", "xxl"); //this is supposed to cause an error
        executeSQLQueryAction.validate("STATUS", "in_progress");

        try {
            executeSQLQueryAction.build().execute(context);
        } catch (ValidationException e) {
            Assert.assertNull(context.getVariables().get("${ORDERTYPE}"));
            Assert.assertNull(context.getVariables().get("${STATUS}"));

            return;
        }

        Assert.fail("Expected test to fail with " + ValidationException.class + " but was successful");
    }

    @Test
    public void testResultSetMultipleRowsValidation() {
        String sql = "select ORDERTYPE, STATUS from orders where ID < 5";
        reset(jdbcTemplate);

        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Object> resultRow1 = new HashMap<>();
        Map<String, Object> resultRow2 = new HashMap<>();
        Map<String, Object> resultRow3 = new HashMap<>();

        resultRow1.put("ORDERTYPE", "small");
        resultRow1.put("STATUS", "started");
        resultList.add(resultRow1);
        resultRow2.put("ORDERTYPE", "medium");
        resultRow2.put("STATUS", "in_progress");
        resultList.add(resultRow2);
        resultRow3.put("ORDERTYPE", "big");
        resultRow3.put("STATUS", "finished");
        resultList.add(resultRow3);

        when(jdbcTemplate.queryForList(sql)).thenReturn(resultList);

        executeSQLQueryAction.statements(Collections.singletonList(sql));
        executeSQLQueryAction.validate("ORDERTYPE", "small", "medium", "big");
        executeSQLQueryAction.validate("STATUS", "started", "in_progress", "finished");
        executeSQLQueryAction.build().execute(context);
    }

    @Test
    public void testNullValuesInMultipleRowsValidation() {
        String sql = "select ORDERTYPE, STATUS from orders where ID < 5";
        reset(jdbcTemplate);

        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Object> resultRow1 = new HashMap<>();
        Map<String, Object> resultRow2 = new HashMap<>();
        Map<String, Object> resultRow3 = new HashMap<>();

        resultRow1.put("ORDERTYPE", "small");
        resultRow1.put("STATUS", null);
        resultList.add(resultRow1);
        resultRow2.put("ORDERTYPE", "medium");
        resultRow2.put("STATUS", "in_progress");
        resultList.add(resultRow2);
        resultRow3.put("ORDERTYPE", null);
        resultRow3.put("STATUS", "finished");
        resultList.add(resultRow3);

        when(jdbcTemplate.queryForList(sql)).thenReturn(resultList);

        executeSQLQueryAction.statements(Collections.singletonList(sql));
        executeSQLQueryAction.validate("ORDERTYPE", "small", "medium", ""); // 1st possibility to validate null values
        executeSQLQueryAction.validate("STATUS", "NULL", "in_progress", "finished"); // 2nd possibility to validate null values
        executeSQLQueryAction.build().execute(context);
    }

    @Test
    public void testIgnoreInMultipleRowsValidation() {
        String sql = "select ORDERTYPE, STATUS from orders where ID < 5";
        reset(jdbcTemplate);

        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Object> resultRow1 = new HashMap<>();
        Map<String, Object> resultRow2 = new HashMap<>();
        Map<String, Object> resultRow3 = new HashMap<>();

        resultRow1.put("ORDERTYPE", "small");
        resultRow1.put("STATUS", "started");
        resultList.add(resultRow1);
        resultRow2.put("ORDERTYPE", "medium");
        resultRow2.put("STATUS", "in_progress");
        resultList.add(resultRow2);
        resultRow3.put("ORDERTYPE", "big");
        resultRow3.put("STATUS", "finished");
        resultList.add(resultRow3);

        when(jdbcTemplate.queryForList(sql)).thenReturn(resultList);

        executeSQLQueryAction.statements(Collections.singletonList(sql));
        executeSQLQueryAction.validate("ORDERTYPE", "small", CitrusSettings.IGNORE_PLACEHOLDER, "big");
        executeSQLQueryAction.validate("STATUS", CitrusSettings.IGNORE_PLACEHOLDER, "in_progress", "finished");
        executeSQLQueryAction.build().execute(context);
    }

    @Test
    public void testExtractMultipleRowValues() {
        String sql = "select distinct STATUS from orders";
        reset(jdbcTemplate);

        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Object> resultRow1 = new HashMap<>();
        Map<String, Object> resultRow2 = new HashMap<>();
        Map<String, Object> resultRow3 = new HashMap<>();

        resultRow1.put("ORDERTYPE", "small");
        resultRow1.put("STATUS", "started");
        resultList.add(resultRow1);
        resultRow2.put("ORDERTYPE", null);
        resultRow2.put("STATUS", "in_progress");
        resultList.add(resultRow2);
        resultRow3.put("ORDERTYPE", "big");
        resultRow3.put("STATUS", "finished");
        resultList.add(resultRow3);

        when(jdbcTemplate.queryForList(sql)).thenReturn(resultList);

        executeSQLQueryAction.statements(Collections.singletonList(sql));
        executeSQLQueryAction.extract("STATUS", "orderStatus");
        executeSQLQueryAction.extract("ORDERTYPE", "orderType");
        executeSQLQueryAction.validate("ORDERTYPE", "small", CitrusSettings.IGNORE_PLACEHOLDER, "big");
        executeSQLQueryAction.validate("STATUS", "started", "in_progress", "finished");
        executeSQLQueryAction.build().execute(context);

        Assert.assertNotNull(context.getVariable("orderType"));
        Assert.assertEquals(context.getVariable("orderType"), "small;NULL;big");
        Assert.assertNotNull(context.getVariable("orderStatus"));
        Assert.assertEquals(context.getVariable("orderStatus"), "started;in_progress;finished");
    }

    @Test
    public void testMultipleStatementsValidationError() {
        String sql1 = DB_STMT_1;
        String sql2 = DB_STMT_2;
        reset(jdbcTemplate);

        Map<String, Object> resultMap1 = new HashMap<>();
        resultMap1.put("ORDERTYPE", "small");
        resultMap1.put("STATUS", "in_progress");

        when(jdbcTemplate.queryForList(sql1)).thenReturn(Collections.singletonList(resultMap1));

        Map<String, Object> resultMap2 = new HashMap<>();
        resultMap2.put("NAME", "Mickey Mouse");
        resultMap2.put("HEIGHT", "0,3");

        when(jdbcTemplate.queryForList(sql2)).thenReturn(Collections.singletonList(resultMap2));

        List<String> statements = new ArrayList<String>();
        statements.add(sql1);
        statements.add(sql2);

        executeSQLQueryAction.statements(statements);
        executeSQLQueryAction.validate("ORDERTYPE", "small");
        executeSQLQueryAction.validate("STATUS", "in_progress");
        executeSQLQueryAction.validate("NAME", "Donald Duck"); //this is supposed to cause an error
        executeSQLQueryAction.validate("HEIGHT", "0,3");

        try {
            executeSQLQueryAction.build().execute(context);
        } catch (ValidationException e) {
            Assert.assertNull(context.getVariables().get("${ORDERTYPE}"));
            Assert.assertNull(context.getVariables().get("${STATUS}"));
            Assert.assertNull(context.getVariables().get("${NAME}"));
            Assert.assertNull(context.getVariables().get("${HEIGHT}"));

            return;
        }

        Assert.fail("Expected test to fail with " + ValidationException.class + " but was successful");
    }

    @Test
    public void testSQLStatementsWithFileResource() {
        String sql1 = DB_STMT_1;
        String sql2 = "select NAME, HEIGHT\nfrom customers\nwhere ID=1";
        reset(jdbcTemplate);

        Map<String, Object> resultMap1 = new HashMap<>();
        resultMap1.put("ORDERTYPE", "small");
        resultMap1.put("STATUS", "in_progress");

        when(jdbcTemplate.queryForList(sql1)).thenReturn(Collections.singletonList(resultMap1));

        Map<String, Object> resultMap2 = new HashMap<>();
        resultMap2.put("NAME", "Mickey Mouse");
        resultMap2.put("HEIGHT", "0,3");

        when(jdbcTemplate.queryForList(sql2)).thenReturn(Collections.singletonList(resultMap2));

        executeSQLQueryAction.sqlResource("classpath:org/citrusframework/actions/test-sql-query-statements.sql");
        executeSQLQueryAction.extract("ORDERTYPE", "orderType");
        executeSQLQueryAction.extract("STATUS", "status");
        executeSQLQueryAction.extract("NAME", "name");
        executeSQLQueryAction.extract("HEIGHT", "height");
        executeSQLQueryAction.build().execute(context);

        Assert.assertNotNull(context.getVariable("${orderType}"));
        Assert.assertEquals(context.getVariable("${orderType}"), "small");
        Assert.assertNotNull(context.getVariable("${status}"));
        Assert.assertEquals(context.getVariable("${status}"), "in_progress");
        Assert.assertNotNull(context.getVariable("${name}"));
        Assert.assertEquals(context.getVariable("${name}"), "Mickey Mouse");
        Assert.assertNotNull(context.getVariable("${height}"));
        Assert.assertEquals(context.getVariable("${height}"), "0,3");
    }

    @Test
    public void testResultSetScriptValidation() {
        String sql = "select ORDERTYPES, STATUS from orders where ID=5";
        reset(jdbcTemplate);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("ORDERTYPE", "small");
        resultMap.put("STATUS", "in_progress");

        when(jdbcTemplate.queryForList(sql)).thenReturn(Collections.singletonList(resultMap));

        executeSQLQueryAction.statements(Collections.singletonList(sql));

        String validationScript = "assert rows.size() == 1\n" +
                "assert rows[0].ORDERTYPE == 'small'\n" +
                "assert rows[0] == [ORDERTYPE:'small', STATUS:'in_progress']";
        executeSQLQueryAction.validateScript(validationScript, ScriptTypes.GROOVY);
        executeSQLQueryAction.build().execute(context);

        verify(resultSetScriptValidator).validateSqlResultSet(any(List.class), any(ScriptValidationContext.class), eq(context));
    }

    @Test
    public void testResultSetScriptValidationMultipleStatements() {
        String sql1 = "select ORDERTYPES, STATUS from orders where ID=5";
        String sql2 = "select ERRORTYPES from types";
        reset(jdbcTemplate);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("ORDERTYPE", "small");
        resultMap.put("STATUS", "in_progress");

        List<Map<String, Object>> results = new ArrayList<>();
        for (int i = 1; i < 4; i++) {
            Map<String, Object> columnMap = new HashMap<>();
            columnMap.put("ID", String.valueOf(i));
            columnMap.put("NAME", "error" + i);

            results.add(columnMap);
        }

        when(jdbcTemplate.queryForList(sql1)).thenReturn(Collections.singletonList(resultMap));
        when(jdbcTemplate.queryForList(sql2)).thenReturn(results);

        List<String> statements = new ArrayList<String>();
        statements.add(sql1);
        statements.add(sql2);
        executeSQLQueryAction.statements(statements);

        String validationScript = "assert rows.size() == 4\n" +
                "assert rows[0].ORDERTYPE == 'small'\n" +
                "assert rows[0] == [ORDERTYPE:'small', STATUS:'in_progress']\n" +
                "assert rows[1].ID == '1'\n" +
                "assert rows[3].NAME == 'error3'\n";
        executeSQLQueryAction.validateScript(validationScript, ScriptTypes.GROOVY);
        executeSQLQueryAction.build().execute(context);

        verify(resultSetScriptValidator).validateSqlResultSet(any(List.class), any(ScriptValidationContext.class), eq(context));
    }

    @Test
    public void testResultSetScriptValidationWrongValue() {
        String sql = "select ORDERTYPES, STATUS from orders where ID=5";
        reset(jdbcTemplate);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("ORDERTYPE", "small");
        resultMap.put("STATUS", "in_progress");

        when(jdbcTemplate.queryForList(sql)).thenReturn(Collections.singletonList(resultMap));

        executeSQLQueryAction.statements(Collections.singletonList(sql));

        String validationScript = "assert rows.size() == 1\n" +
                "assert rows[0] == [ORDERTYPE:'big', STATUS:'in_progress']";
        executeSQLQueryAction.groovy(validationScript);

        doThrow(new ValidationException("Failed validation script", new AssertionError())).when(resultSetScriptValidator).validateSqlResultSet(any(List.class), any(ScriptValidationContext.class), eq(context));

        try {
            executeSQLQueryAction.build().execute(context);
        } catch (ValidationException e) {
            Assert.assertTrue(e.getCause() instanceof AssertionError);
            return;
        }

        Assert.fail("Missing validation exception due to script validation error");
        verify(resultSetScriptValidator).validateSqlResultSet(any(List.class), any(ScriptValidationContext.class), eq(context));
    }

    @Test
    public void testResultSetScriptValidationCombination() {
        String sql = "select ORDERTYPES, STATUS from orders where ID=5";
        reset(jdbcTemplate);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("ORDERTYPE", "small");
        resultMap.put("STATUS", "in_progress");

        when(jdbcTemplate.queryForList(sql)).thenReturn(Collections.singletonList(resultMap));

        executeSQLQueryAction.statements(Collections.singletonList(sql));
        executeSQLQueryAction.validate("ORDERTYPE", "small");
        executeSQLQueryAction.validate("STATUS", "in_progress");

        String validationScript = "assert rows.size() == 1\n" +
                "assert rows[0].ORDERTYPE == 'small'\n" +
                "assert rows[0] == [ORDERTYPE:'small', STATUS:'in_progress']";
        executeSQLQueryAction.validateScript(validationScript, ScriptTypes.GROOVY);
        executeSQLQueryAction.build().execute(context);

        verify(resultSetScriptValidator).validateSqlResultSet(any(List.class), any(ScriptValidationContext.class), eq(context));
    }

    @Test
    public void testResultSetValidationWithVariableAndFunction() {
        String sql = DB_STMT_1;
        reset(jdbcTemplate);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("ORDERTYPE", "testVariableValue");
        resultMap.put("STATUS", "in_progress");

        when(jdbcTemplate.queryForList(sql)).thenReturn(Collections.singletonList(resultMap));

        executeSQLQueryAction.statements(Collections.singletonList(sql));

        executeSQLQueryAction.validate("ORDERTYPE", "${testVariable}");
        executeSQLQueryAction.validate("STATUS", "citrus:concat('in_', ${progressVar})");

        context.getVariables().put("testVariable", "testVariableValue");
        context.getVariables().put("progressVar", "progress");
        executeSQLQueryAction.build().execute(context);
    }

    @Test
    public void testBinaryBlobColumnValues() {
        String sql = "select ORDERTYPE, BINARY_DATA from orders where ID=5";
        reset(jdbcTemplate);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("ORDERTYPE", "small");
        resultMap.put("BINARY_DATA", "some_binary_data".getBytes());

        when(jdbcTemplate.queryForList(sql)).thenReturn(Collections.singletonList(resultMap));

        executeSQLQueryAction.statements(Collections.singletonList(sql));
        executeSQLQueryAction.extract("BINARY_DATA", "binaryData");
        executeSQLQueryAction.build().execute(context);

        Assert.assertNotNull(context.getVariable("${binaryData}"));
        Assert.assertEquals(context.getVariable("${binaryData}"), Base64.encodeBase64String("some_binary_data".getBytes()));
        Assert.assertEquals(new String(Base64.decodeBase64(context.getVariable("${binaryData}"))), "some_binary_data");
    }

    @Test
    public void testNoJdbcTemplateConfigured() {
        // Special ExecuteSQLQueryAction without a JdbcTemplate
        executeSQLQueryAction = new ExecuteSQLQueryAction.Builder().jdbcTemplate(null);
        executeSQLQueryAction.statements(Collections.singletonList("statement"));

        CitrusRuntimeException exception = Assert.expectThrows(CitrusRuntimeException.class, () -> executeSQLQueryAction.build().execute(context));

        Assert.assertEquals(exception.getMessage(), "No JdbcTemplate configured for query execution!");
    }
}
