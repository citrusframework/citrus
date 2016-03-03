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

package com.consol.citrus.actions;

import com.consol.citrus.Citrus;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.script.ScriptTypes;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.script.ScriptValidationContext;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;

import static org.mockito.Mockito.*;


/**
 * @author Christoph Deppisch
 */
public class ExecuteSQLQueryActionTest extends AbstractTestNGUnitTest {
	
    private ExecuteSQLQueryAction executeSQLQueryAction;
    
    private JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
    
    @BeforeMethod
    public void setUp() {
        executeSQLQueryAction  = new ExecuteSQLQueryAction();
        executeSQLQueryAction.setJdbcTemplate(jdbcTemplate);
    }
    
	@Test
	public void testSQLStatement() {
	    String sql = "select ORDERTYPE, STATUS from orders where ID=5";
	    reset(jdbcTemplate);
	    
	    Map<String, Object> resultMap = new HashMap<String, Object>();
	    resultMap.put("ORDERTYPE", "small");
	    resultMap.put("STATUS", "in_progress");
	    
	    when(jdbcTemplate.queryForList(sql)).thenReturn(Collections.singletonList(resultMap));
	    
	    List<String> stmts = Collections.singletonList(sql);
	    executeSQLQueryAction.setStatements(stmts);
	    
	    executeSQLQueryAction.execute(context);

	    Assert.assertNotNull(context.getVariable("${ORDERTYPE}"));
	    Assert.assertEquals(context.getVariable("${ORDERTYPE}"), "small");
	    Assert.assertNotNull(context.getVariable("${STATUS}"));
        Assert.assertEquals(context.getVariable("${STATUS}"), "in_progress");
	}

    @Test
    public void testSQLStatementLowerCaseColumnNames() {
        String sql = "select ORDERTYPE, STATUS from orders where ID=5";
        reset(jdbcTemplate);

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("ordertype", "small");
        resultMap.put("status", "in_progress");

        when(jdbcTemplate.queryForList(sql)).thenReturn(Collections.singletonList(resultMap));

                List<String> stmts = Collections.singletonList(sql);
        executeSQLQueryAction.setStatements(stmts);

        executeSQLQueryAction.execute(context);

        Assert.assertNotNull(context.getVariable("${ORDERTYPE}"));
        Assert.assertEquals(context.getVariable("${ORDERTYPE}"), "small");
        Assert.assertNotNull(context.getVariable("${STATUS}"));
        Assert.assertEquals(context.getVariable("${STATUS}"), "in_progress");
    }
	
	@Test
    public void testSQLMultipleStatements() {
        String sql1 = "select ORDERTYPE, STATUS from orders where ID=5";
        String sql2 = "select NAME, HEIGHT from customers where ID=1";
        reset(jdbcTemplate);
        
        Map<String, Object> resultMap1 = new HashMap<String, Object>();
        resultMap1.put("ORDERTYPE", "small");
        resultMap1.put("STATUS", "in_progress");
        
        when(jdbcTemplate.queryForList(sql1)).thenReturn(Collections.singletonList(resultMap1));

        Map<String, Object> resultMap2 = new HashMap<String, Object>();
        resultMap2.put("NAME", "Mickey Mouse");
        resultMap2.put("HEIGHT", "0,3");
        
        when(jdbcTemplate.queryForList(sql2)).thenReturn(Collections.singletonList(resultMap2));

        List<String> stmts = new ArrayList<String>();
        stmts.add(sql1);
        stmts.add(sql2);
        
        executeSQLQueryAction.setStatements(stmts);
        
        executeSQLQueryAction.execute(context);

        Assert.assertNotNull(context.getVariable("${ORDERTYPE}"));
        Assert.assertEquals(context.getVariable("${ORDERTYPE}"), "small");
        Assert.assertNotNull(context.getVariable("${STATUS}"));
        Assert.assertEquals(context.getVariable("${STATUS}"), "in_progress");
        Assert.assertNotNull(context.getVariable("${NAME}"));
        Assert.assertEquals(context.getVariable("${NAME}"), "Mickey Mouse");
        Assert.assertNotNull(context.getVariable("${HEIGHT}"));
        Assert.assertEquals(context.getVariable("${HEIGHT}"), "0,3");
    }
	
	@Test
    public void testSQLResource() {
	    String sql1 = "SELECT ORDERTYPE, STATUS FROM orders WHERE ID=5";
        String sql2 = "SELECT NAME, HEIGHT FROM customers WHERE ID=1";
        reset(jdbcTemplate);
        
        Map<String, Object> resultMap1 = new HashMap<String, Object>();
        resultMap1.put("ORDERTYPE", "small");
        resultMap1.put("STATUS", "in_progress");
        
        when(jdbcTemplate.queryForList(sql1)).thenReturn(Collections.singletonList(resultMap1));
        
        Map<String, Object> resultMap2 = new HashMap<String, Object>();
        resultMap2.put("NAME", "Mickey Mouse");
        resultMap2.put("HEIGHT", "0,3");
        
        when(jdbcTemplate.queryForList(sql2)).thenReturn(Collections.singletonList(resultMap2));

        executeSQLQueryAction.setSqlResourcePath("classpath:com/consol/citrus/actions/test-query.sql");
        
        executeSQLQueryAction.execute(context);

        Assert.assertNotNull(context.getVariable("${ORDERTYPE}"));
        Assert.assertEquals(context.getVariable("${ORDERTYPE}"), "small");
        Assert.assertNotNull(context.getVariable("${STATUS}"));
        Assert.assertEquals(context.getVariable("${STATUS}"), "in_progress");
        Assert.assertNotNull(context.getVariable("${NAME}"));
        Assert.assertEquals(context.getVariable("${NAME}"), "Mickey Mouse");
        Assert.assertNotNull(context.getVariable("${HEIGHT}"));
        Assert.assertEquals(context.getVariable("${HEIGHT}"), "0,3");
    }
	
	@Test
    public void testNullValue() {
        String sql = "select ORDERTYPE, STATUS from orders where ID=5";
        reset(jdbcTemplate);
        
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("ORDERTYPE", "small");
        resultMap.put("STATUS", null);
        
        when(jdbcTemplate.queryForList(sql)).thenReturn(Collections.singletonList(resultMap));

        List<String> stmts = Collections.singletonList(sql);
        executeSQLQueryAction.setStatements(stmts);
        
        executeSQLQueryAction.execute(context);

        Assert.assertNotNull(context.getVariable("${ORDERTYPE}"));
        Assert.assertEquals(context.getVariable("${ORDERTYPE}"), "small");
        Assert.assertNotNull(context.getVariable("${STATUS}"));
        Assert.assertEquals(context.getVariable("${STATUS}"), "NULL");
    }
	
	@Test
    public void testVariableSupport() {
	    context.setVariable("orderId", "5");
	    
        String sql = "select ORDERTYPE, STATUS from orders where ID=${orderId}";
        reset(jdbcTemplate);
        
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("ORDERTYPE", "small");
        resultMap.put("STATUS", "in_progress");
        
        when(jdbcTemplate.queryForList("select ORDERTYPE, STATUS from orders where ID=5")).thenReturn(Collections.singletonList(resultMap));

        List<String> stmts = Collections.singletonList(sql);
        executeSQLQueryAction.setStatements(stmts);
        
        executeSQLQueryAction.execute(context);

        Assert.assertNotNull(context.getVariable("${ORDERTYPE}"));
        Assert.assertEquals(context.getVariable("${ORDERTYPE}"), "small");
        Assert.assertNotNull(context.getVariable("${STATUS}"));
        Assert.assertEquals(context.getVariable("${STATUS}"), "in_progress");
    }
	
	@Test
    public void testExtractToVariables() {
	    String sql = "select ORDERTYPE, STATUS from orders where ID=5";
        reset(jdbcTemplate);
        
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("ORDERTYPE", "small");
        resultMap.put("STATUS", "in_progress");
        
        when(jdbcTemplate.queryForList(sql)).thenReturn(Collections.singletonList(resultMap));

        List<String> stmts = Collections.singletonList(sql);
        executeSQLQueryAction.setStatements(stmts);
        
        Map<String, String> extractVariables = new HashMap<String, String>();
        extractVariables.put("STATUS", "orderStatus");
        executeSQLQueryAction.setExtractVariables(extractVariables);
        
        executeSQLQueryAction.execute(context);

        Assert.assertNotNull(context.getVariable("${orderStatus}"));
        Assert.assertEquals(context.getVariable("${orderStatus}"), "in_progress");
        Assert.assertNotNull(context.getVariable("${ORDERTYPE}"));
        Assert.assertEquals(context.getVariable("${ORDERTYPE}"), "small");
        Assert.assertNotNull(context.getVariable("${STATUS}"));
        Assert.assertEquals(context.getVariable("${STATUS}"), "in_progress");
    }

    @Test
    public void testExtractToVariablesLowerCaseColumnNames() {
        String sql = "select ORDERTYPE, STATUS from orders where ID=5";
        reset(jdbcTemplate);

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("ordertype", "small");
        resultMap.put("status", "in_progress");

        when(jdbcTemplate.queryForList(sql)).thenReturn(Collections.singletonList(resultMap));

        List<String> stmts = Collections.singletonList(sql);
        executeSQLQueryAction.setStatements(stmts);

        Map<String, String> extractVariables = new HashMap<String, String>();
        extractVariables.put("ordertype", "orderType");
        extractVariables.put("STATUS", "orderStatus");
        executeSQLQueryAction.setExtractVariables(extractVariables);

        executeSQLQueryAction.execute(context);

        Assert.assertNotNull(context.getVariable("${orderStatus}"));
        Assert.assertEquals(context.getVariable("${orderStatus}"), "in_progress");
        Assert.assertNotNull(context.getVariable("${ORDERTYPE}"));
        Assert.assertEquals(context.getVariable("${ORDERTYPE}"), "small");
        Assert.assertNotNull(context.getVariable("${orderType}"));
        Assert.assertEquals(context.getVariable("${orderType}"), "small");
        Assert.assertNotNull(context.getVariable("${STATUS}"));
        Assert.assertEquals(context.getVariable("${STATUS}"), "in_progress");
    }
	
	@Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testExtractToVariablesUnknownColumnMapping() {
        String sql = "select ORDERTYPE, STATUS from orders where ID=5";
        reset(jdbcTemplate);
        
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("ORDERTYPE", "small");
        resultMap.put("STATUS", "in_progress");
        
        when(jdbcTemplate.queryForList(sql)).thenReturn(Collections.singletonList(resultMap));

        List<String> stmts = Collections.singletonList(sql);
        executeSQLQueryAction.setStatements(stmts);
        
        Map<String, String> extractVariables = new HashMap<String, String>();
        extractVariables.put("UNKNOWN_COLUMN", "orderStatus");
        executeSQLQueryAction.setExtractVariables(extractVariables);
        
        executeSQLQueryAction.execute(context);

    }
	
	@Test
    public void testResultSetValidation() {
        String sql = "select ORDERTYPE, STATUS from orders where ID=5";
        reset(jdbcTemplate);
        
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("ORDERTYPE", "small");
        resultMap.put("STATUS", "in_progress");
        
        when(jdbcTemplate.queryForList(sql)).thenReturn(Collections.singletonList(resultMap));

        List<String> stmts = Collections.singletonList(sql);
        executeSQLQueryAction.setStatements(stmts);
        
        Map<String, List<String>> controlResultSet = new HashMap<String, List<String>>();
        controlResultSet.put("ORDERTYPE", Collections.singletonList("small"));
        controlResultSet.put("STATUS", Collections.singletonList("in_progress"));
        
        executeSQLQueryAction.setControlResultSet(controlResultSet);
        
        executeSQLQueryAction.execute(context);

        
        Assert.assertNotNull(context.getVariable("${ORDERTYPE}"));
        Assert.assertEquals(context.getVariable("${ORDERTYPE}"), "small");
        Assert.assertNotNull(context.getVariable("${STATUS}"));
        Assert.assertEquals(context.getVariable("${STATUS}"), "in_progress");
    }

    @Test
    public void testResultSetValidationLowerCase() {
        String sql = "select ORDERTYPE, STATUS from orders where ID=5";
        reset(jdbcTemplate);

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("ordertype", "small");
        resultMap.put("status", "in_progress");

        when(jdbcTemplate.queryForList(sql)).thenReturn(Collections.singletonList(resultMap));

        List<String> stmts = Collections.singletonList(sql);
        executeSQLQueryAction.setStatements(stmts);

        Map<String, List<String>> controlResultSet = new HashMap<String, List<String>>();
        controlResultSet.put("ORDERTYPE", Collections.singletonList("small"));
        controlResultSet.put("STATUS", Collections.singletonList("in_progress"));

        executeSQLQueryAction.setControlResultSet(controlResultSet);

        executeSQLQueryAction.execute(context);

        Assert.assertNotNull(context.getVariable("${ORDERTYPE}"));
        Assert.assertEquals(context.getVariable("${ORDERTYPE}"), "small");
        Assert.assertNotNull(context.getVariable("${STATUS}"));
        Assert.assertEquals(context.getVariable("${STATUS}"), "in_progress");
    }
	
	@Test
    public void testResultSetValidationWithAliasNames() {
        String sql = "select ORDERTYPE AS TYPE, STATUS AS STATE from orders where ID=5";
        reset(jdbcTemplate);
        
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("TYPE", "small");
        resultMap.put("STATE", "in_progress");
        
        when(jdbcTemplate.queryForList(sql)).thenReturn(Collections.singletonList(resultMap));

        List<String> stmts = Collections.singletonList(sql);
        executeSQLQueryAction.setStatements(stmts);
        
        Map<String, List<String>> controlResultSet = new HashMap<String, List<String>>();
        controlResultSet.put("TYPE", Collections.singletonList("small"));
        controlResultSet.put("STATE", Collections.singletonList("in_progress"));
        
        executeSQLQueryAction.setControlResultSet(controlResultSet);
        
        executeSQLQueryAction.execute(context);

        Assert.assertNotNull(context.getVariable("${TYPE}"));
        Assert.assertEquals(context.getVariable("${TYPE}"), "small");
        Assert.assertNotNull(context.getVariable("${STATE}"));
        Assert.assertEquals(context.getVariable("${STATE}"), "in_progress");
    }
	
	@Test
    public void testResultSetValidationError() {
        String sql = "select ORDERTYPE, STATUS from orders where ID=5";
        reset(jdbcTemplate);
        
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("ORDERTYPE", "small");
        resultMap.put("STATUS", "in_progress");
        
        when(jdbcTemplate.queryForList(sql)).thenReturn(Collections.singletonList(resultMap));

        List<String> stmts = Collections.singletonList(sql);
        executeSQLQueryAction.setStatements(stmts);
        
        Map<String, List<String>> controlResultSet = new HashMap<String, List<String>>();
        controlResultSet.put("ORDERTYPE", Collections.singletonList("xxl")); //this is supposed to cause an error
        controlResultSet.put("STATUS", Collections.singletonList("in_progress"));
        
        executeSQLQueryAction.setControlResultSet(controlResultSet);
        
        try {
            executeSQLQueryAction.execute(context);
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
        
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        Map<String, Object> resultRow1 = new HashMap<String, Object>();
        Map<String, Object> resultRow2 = new HashMap<String, Object>();
        Map<String, Object> resultRow3 = new HashMap<String, Object>();

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

        List<String> stmts = Collections.singletonList(sql);
        executeSQLQueryAction.setStatements(stmts);
        
        Map<String, List<String>> controlResultSet = new HashMap<String, List<String>>();
        List<String> ordertypeValues = new ArrayList<String>();
        ordertypeValues.add("small");
        ordertypeValues.add("medium");
        ordertypeValues.add("big");
        controlResultSet.put("ORDERTYPE", ordertypeValues);
        
        List<String> statusValues = new ArrayList<String>();
        statusValues.add("started");
        statusValues.add("in_progress");
        statusValues.add("finished");
        controlResultSet.put("STATUS", statusValues);
        
        executeSQLQueryAction.setControlResultSet(controlResultSet);
        
        executeSQLQueryAction.execute(context);

        Assert.assertNotNull(context.getVariable("ORDERTYPE"));
        Assert.assertEquals(context.getVariable("ORDERTYPE"), "small");
        Assert.assertNotNull(context.getVariable("STATUS"));
        Assert.assertEquals(context.getVariable("STATUS"), "started");
    }
    
    @Test
    public void testNullValuesInMultipleRowsValidation() {
        String sql = "select ORDERTYPE, STATUS from orders where ID < 5";
        reset(jdbcTemplate);
        
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        Map<String, Object> resultRow1 = new HashMap<String, Object>();
        Map<String, Object> resultRow2 = new HashMap<String, Object>();
        Map<String, Object> resultRow3 = new HashMap<String, Object>();

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

        List<String> stmts = Collections.singletonList(sql);
        executeSQLQueryAction.setStatements(stmts);
        
        Map<String, List<String>> controlResultSet = new HashMap<String, List<String>>();
        List<String> ordertypeValues = new ArrayList<String>();
        ordertypeValues.add("small");
        ordertypeValues.add("medium");
        ordertypeValues.add(""); // 1st possibility to validate null values
        controlResultSet.put("ORDERTYPE", ordertypeValues);
        
        List<String> statusValues = new ArrayList<String>();
        statusValues.add("NULL"); // 2nd possibility to validate null values
        statusValues.add("in_progress");
        statusValues.add("finished");
        controlResultSet.put("STATUS", statusValues);
        
        executeSQLQueryAction.setControlResultSet(controlResultSet);
        
        executeSQLQueryAction.execute(context);

        Assert.assertNotNull(context.getVariable("ORDERTYPE"));
        Assert.assertEquals(context.getVariable("ORDERTYPE"), "small");
        Assert.assertNotNull(context.getVariable("STATUS"));
        Assert.assertEquals(context.getVariable("STATUS"), "NULL");
    }
    
    @Test
    public void testIgnoreInMultipleRowsValidation() {
        String sql = "select ORDERTYPE, STATUS from orders where ID < 5";
        reset(jdbcTemplate);
        
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        Map<String, Object> resultRow1 = new HashMap<String, Object>();
        Map<String, Object> resultRow2 = new HashMap<String, Object>();
        Map<String, Object> resultRow3 = new HashMap<String, Object>();

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

        List<String> stmts = Collections.singletonList(sql);
        executeSQLQueryAction.setStatements(stmts);
        
        Map<String, List<String>> controlResultSet = new HashMap<String, List<String>>();
        List<String> ordertypeValues = new ArrayList<String>();
        ordertypeValues.add("small");
        ordertypeValues.add(Citrus.IGNORE_PLACEHOLDER);
        ordertypeValues.add("big");
        
        controlResultSet.put("ORDERTYPE", ordertypeValues);
        
        List<String> statusValues = new ArrayList<String>();
        statusValues.add(Citrus.IGNORE_PLACEHOLDER);
        statusValues.add("in_progress");
        statusValues.add("finished");
        controlResultSet.put("STATUS", statusValues);
        
        executeSQLQueryAction.setControlResultSet(controlResultSet);
        
        executeSQLQueryAction.execute(context);

        Assert.assertNotNull(context.getVariable("ORDERTYPE"));
        Assert.assertEquals(context.getVariable("ORDERTYPE"), "small");
        Assert.assertNotNull(context.getVariable("STATUS"));
        Assert.assertEquals(context.getVariable("STATUS"), "started");
    }
    
    @Test
    public void testExtractMultipleRowValues() {
        String sql = "select distinct STATUS from orders";
        reset(jdbcTemplate);
        
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        Map<String, Object> resultRow1 = new HashMap<String, Object>();
        Map<String, Object> resultRow2 = new HashMap<String, Object>();
        Map<String, Object> resultRow3 = new HashMap<String, Object>();

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

        List<String> stmts = Collections.singletonList(sql);
        executeSQLQueryAction.setStatements(stmts);
        
        Map<String, String> extractVariables = new HashMap<String, String>();
        extractVariables.put("STATUS", "orderStatus");
        extractVariables.put("ORDERTYPE", "orderType");
        executeSQLQueryAction.setExtractVariables(extractVariables);
        
        Map<String, List<String>> controlResultSet = new HashMap<String, List<String>>();
        List<String> ordertypeValues = new ArrayList<String>();
        ordertypeValues.add("small");
        ordertypeValues.add(Citrus.IGNORE_PLACEHOLDER);
        ordertypeValues.add("big");
        
        controlResultSet.put("ORDERTYPE", ordertypeValues);
        
        List<String> statusValues = new ArrayList<String>();
        statusValues.add("started");
        statusValues.add("in_progress");
        statusValues.add("finished");
        controlResultSet.put("STATUS", statusValues);
        
        executeSQLQueryAction.setControlResultSet(controlResultSet);
        
        executeSQLQueryAction.execute(context);

        Assert.assertNotNull(context.getVariable("orderType"));
        Assert.assertEquals(context.getVariable("orderType"), "small;NULL;big");
        Assert.assertNotNull(context.getVariable("orderStatus"));
        Assert.assertEquals(context.getVariable("orderStatus"), "started;in_progress;finished");
        Assert.assertNotNull(context.getVariable("ORDERTYPE"));
        Assert.assertEquals(context.getVariable("ORDERTYPE"), "small");
        Assert.assertNotNull(context.getVariable("STATUS"));
        Assert.assertEquals(context.getVariable("STATUS"), "started");
    }
    
    @Test
    public void testMultipleStatementsValidationError() {
        String sql1 = "select ORDERTYPE, STATUS from orders where ID=5";
        String sql2 = "select NAME, HEIGHT from customers where ID=1";
        reset(jdbcTemplate);
        
        Map<String, Object> resultMap1 = new HashMap<String, Object>();
        resultMap1.put("ORDERTYPE", "small");
        resultMap1.put("STATUS", "in_progress");
        
        when(jdbcTemplate.queryForList(sql1)).thenReturn(Collections.singletonList(resultMap1));
        
        Map<String, Object> resultMap2 = new HashMap<String, Object>();
        resultMap2.put("NAME", "Mickey Mouse");
        resultMap2.put("HEIGHT", "0,3");
        
        when(jdbcTemplate.queryForList(sql2)).thenReturn(Collections.singletonList(resultMap2));

        List<String> stmts = new ArrayList<String>();
        stmts.add(sql1);
        stmts.add(sql2);
        
        executeSQLQueryAction.setStatements(stmts);
        
        Map<String, List<String>> controlResultSet = new HashMap<String, List<String>>();
        controlResultSet.put("ORDERTYPE", Collections.singletonList("small"));
        controlResultSet.put("STATUS", Collections.singletonList("in_progress"));
        controlResultSet.put("NAME", Collections.singletonList("Donald Duck")); //this is supposed to cause an error
        controlResultSet.put("HEIGHT", Collections.singletonList("0,3"));
        
        executeSQLQueryAction.setControlResultSet(controlResultSet);
        
        try {
            executeSQLQueryAction.execute(context);
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
        String sql1 = "select ORDERTYPE, STATUS from orders where ID=5";
        String sql2 = "select NAME, HEIGHT\nfrom customers\nwhere ID=1";
        reset(jdbcTemplate);
        
        Map<String, Object> resultMap1 = new HashMap<String, Object>();
        resultMap1.put("ORDERTYPE", "small");
        resultMap1.put("STATUS", "in_progress");
        
        when(jdbcTemplate.queryForList(sql1)).thenReturn(Collections.singletonList(resultMap1));
        
        Map<String, Object> resultMap2 = new HashMap<String, Object>();
        resultMap2.put("NAME", "Mickey Mouse");
        resultMap2.put("HEIGHT", "0,3");
        
        when(jdbcTemplate.queryForList(sql2)).thenReturn(Collections.singletonList(resultMap2));

        executeSQLQueryAction.setSqlResourcePath("classpath:com/consol/citrus/actions/test-sql-query-statements.sql");
        
        executeSQLQueryAction.execute(context);

        Assert.assertNotNull(context.getVariable("${ORDERTYPE}"));
        Assert.assertEquals(context.getVariable("${ORDERTYPE}"), "small");
        Assert.assertNotNull(context.getVariable("${STATUS}"));
        Assert.assertEquals(context.getVariable("${STATUS}"), "in_progress");
        Assert.assertNotNull(context.getVariable("${NAME}"));
        Assert.assertEquals(context.getVariable("${NAME}"), "Mickey Mouse");
        Assert.assertNotNull(context.getVariable("${HEIGHT}"));
        Assert.assertEquals(context.getVariable("${HEIGHT}"), "0,3");
    }
    
    @Test
    public void testResultSetScriptValidation() {
        String sql = "select ORDERTYPES, STATUS from orders where ID=5";
        reset(jdbcTemplate);
        
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("ORDERTYPE", "small");
        resultMap.put("STATUS", "in_progress");
        
        when(jdbcTemplate.queryForList(sql)).thenReturn(Collections.singletonList(resultMap));

        List<String> stmts = Collections.singletonList(sql);
        executeSQLQueryAction.setStatements(stmts);
        
        ScriptValidationContext scriptValidationContext = new ScriptValidationContext(ScriptTypes.GROOVY);
        scriptValidationContext.setValidationScript("assert rows.size() == 1\n" +
                "assert rows[0].ORDERTYPE == 'small'\n" +
                "assert rows[0] == [ORDERTYPE:'small', STATUS:'in_progress']");
        executeSQLQueryAction.setScriptValidationContext(scriptValidationContext);
        
        executeSQLQueryAction.execute(context);

        Assert.assertNotNull(context.getVariable("${ORDERTYPE}"));
        Assert.assertEquals(context.getVariable("${ORDERTYPE}"), "small");
        Assert.assertNotNull(context.getVariable("${STATUS}"));
        Assert.assertEquals(context.getVariable("${STATUS}"), "in_progress");
    }
    
    @Test
    public void testResultSetScriptValidationMultipleStmts() {
        String sql1 = "select ORDERTYPES, STATUS from orders where ID=5";
        String sql2 = "select ERRORTYPES from types";
        reset(jdbcTemplate);
        
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("ORDERTYPE", "small");
        resultMap.put("STATUS", "in_progress");
        
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        for (int i = 1; i < 4; i++) {
            Map<String, Object> columnMap = new HashMap<String, Object>();
            columnMap.put("ID", String.valueOf(i));
            columnMap.put("NAME", "error" + i);
            
            results.add(columnMap);
        }
        
        when(jdbcTemplate.queryForList(sql1)).thenReturn(Collections.singletonList(resultMap));
        when(jdbcTemplate.queryForList(sql2)).thenReturn(results);

        List<String> stmts = new ArrayList<String>();
        stmts.add(sql1);
        stmts.add(sql2);
        executeSQLQueryAction.setStatements(stmts);
        
        ScriptValidationContext scriptValidationContext = new ScriptValidationContext(ScriptTypes.GROOVY);
        scriptValidationContext.setValidationScript("assert rows.size() == 4\n" +
                "assert rows[0].ORDERTYPE == 'small'\n" +
                "assert rows[0] == [ORDERTYPE:'small', STATUS:'in_progress']\n" +
                "assert rows[1].ID == '1'\n" +
                "assert rows[3].NAME == 'error3'\n");
        executeSQLQueryAction.setScriptValidationContext(scriptValidationContext);
        
        executeSQLQueryAction.execute(context);

    }
    
    @Test
    public void testResultSetScriptValidationWrongValue() {
        String sql = "select ORDERTYPES, STATUS from orders where ID=5";
        reset(jdbcTemplate);
        
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("ORDERTYPE", "small");
        resultMap.put("STATUS", "in_progress");
        
        when(jdbcTemplate.queryForList(sql)).thenReturn(Collections.singletonList(resultMap));

        List<String> stmts = Collections.singletonList(sql);
        executeSQLQueryAction.setStatements(stmts);
        
        ScriptValidationContext scriptValidationContext = new ScriptValidationContext(ScriptTypes.GROOVY);
        scriptValidationContext.setValidationScript("assert rows.size() == 1\n" +
                "assert rows[0] == [ORDERTYPE:'big', STATUS:'in_progress']");
        executeSQLQueryAction.setScriptValidationContext(scriptValidationContext);
        
        try {
            executeSQLQueryAction.execute(context);
        } catch (ValidationException e) {
            Assert.assertTrue(e.getCause() instanceof AssertionError);
            return;
        }

        
        Assert.fail("Missing validation exception due to script validation error");
    }
    
    @Test
    public void testResultSetScriptValidationCombination() {
        String sql = "select ORDERTYPES, STATUS from orders where ID=5";
        reset(jdbcTemplate);
        
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("ORDERTYPE", "small");
        resultMap.put("STATUS", "in_progress");
        
        when(jdbcTemplate.queryForList(sql)).thenReturn(Collections.singletonList(resultMap));

        List<String> stmts = Collections.singletonList(sql);
        executeSQLQueryAction.setStatements(stmts);
        
        Map<String, List<String>> controlResultSet = new HashMap<String, List<String>>();
        controlResultSet.put("ORDERTYPE", Collections.singletonList("small"));
        controlResultSet.put("STATUS", Collections.singletonList("in_progress"));
        
        executeSQLQueryAction.setControlResultSet(controlResultSet);
        
        ScriptValidationContext scriptValidationContext = new ScriptValidationContext(ScriptTypes.GROOVY);
        scriptValidationContext.setValidationScript("assert rows.size() == 1\n" +
                "assert rows[0].ORDERTYPE == 'small'\n" +
                "assert rows[0] == [ORDERTYPE:'small', STATUS:'in_progress']");
        executeSQLQueryAction.setScriptValidationContext(scriptValidationContext);
        
        executeSQLQueryAction.execute(context);
    }

    @Test
    public void testResultSetValidationWithVariableAndFunction() {
        String sql = "select ORDERTYPE, STATUS from orders where ID=5";
        reset(jdbcTemplate);

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("ORDERTYPE", "testVariableValue");
        resultMap.put("STATUS", "in_progress");

        when(jdbcTemplate.queryForList(sql)).thenReturn(Collections.singletonList(resultMap));

        List<String> stmts = Collections.singletonList(sql);
        executeSQLQueryAction.setStatements(stmts);

        Map<String, List<String>> controlResultSet = new HashMap<String, List<String>>();
        controlResultSet.put("ORDERTYPE", Collections.singletonList("${testVariable}"));
        controlResultSet.put("STATUS", Collections.singletonList("citrus:concat('in_', ${progressVar})"));

        executeSQLQueryAction.setControlResultSet(controlResultSet);

        context.getVariables().put("testVariable", "testVariableValue");
        context.getVariables().put("progressVar", "progress");
        executeSQLQueryAction.execute(context);
    }
}
