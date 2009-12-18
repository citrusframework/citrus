/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.actions;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;

import java.util.*;

import org.easymock.classextension.EasyMock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testng.Assert;
import org.testng.annotations.*;

import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.testng.AbstractBaseTest;

public class ExecuteSQLQueryActionTest extends AbstractBaseTest {
	
    private ExecuteSQLQueryAction executeSQLQueryAction;
    
    private JdbcTemplate jdbcTemplate = EasyMock.createMock(JdbcTemplate.class);
    
    @BeforeMethod
    public void setUp() {
        executeSQLQueryAction  = new ExecuteSQLQueryAction();
        executeSQLQueryAction.setJdbcTemplate(jdbcTemplate);
    }
    
	@Test
	public void testSQLStatement() {
	    String sql = "select ORDERTYPE, STATUS from orders where ID=5";
	    reset(jdbcTemplate);
	    
	    Map<String, String> resultMap = new HashMap<String, String>();
	    resultMap.put("ORDERTYPE", "small");
	    resultMap.put("STATUS", "in_progress");
	    
	    expect(jdbcTemplate.queryForList(sql)).andReturn(Collections.singletonList(resultMap));
	    
	    replay(jdbcTemplate);
	    
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
        
        Map<String, String> resultMap1 = new HashMap<String, String>();
        resultMap1.put("ORDERTYPE", "small");
        resultMap1.put("STATUS", "in_progress");
        
        expect(jdbcTemplate.queryForList(sql1)).andReturn(Collections.singletonList(resultMap1));
        
        Map<String, String> resultMap2 = new HashMap<String, String>();
        resultMap2.put("NAME", "Mickey Mouse");
        resultMap2.put("HEIGHT", "0,3");
        
        expect(jdbcTemplate.queryForList(sql2)).andReturn(Collections.singletonList(resultMap2));
        
        replay(jdbcTemplate);
        
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
    public void testNullValue() {
        String sql = "select ORDERTYPE, STATUS from orders where ID=5";
        reset(jdbcTemplate);
        
        Map<String, String> resultMap = new HashMap<String, String>();
        resultMap.put("ORDERTYPE", "small");
        resultMap.put("STATUS", null);
        
        expect(jdbcTemplate.queryForList(sql)).andReturn(Collections.singletonList(resultMap));
        
        replay(jdbcTemplate);
        
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
        
        Map<String, String> resultMap = new HashMap<String, String>();
        resultMap.put("ORDERTYPE", "small");
        resultMap.put("STATUS", "in_progress");
        
        expect(jdbcTemplate.queryForList("select ORDERTYPE, STATUS from orders where ID=5")).andReturn(Collections.singletonList(resultMap));
        
        replay(jdbcTemplate);
        
        List<String> stmts = Collections.singletonList(sql);
        executeSQLQueryAction.setStatements(stmts);
        
        executeSQLQueryAction.execute(context);
        
        Assert.assertNotNull(context.getVariable("${ORDERTYPE}"));
        Assert.assertEquals(context.getVariable("${ORDERTYPE}"), "small");
        Assert.assertNotNull(context.getVariable("${STATUS}"));
        Assert.assertEquals(context.getVariable("${STATUS}"), "in_progress");
    }
	
	@Test
    public void testResultSetValidation() {
        String sql = "select ORDERTYPE, STATUS from orders where ID=5";
        reset(jdbcTemplate);
        
        Map<String, String> resultMap = new HashMap<String, String>();
        resultMap.put("ORDERTYPE", "small");
        resultMap.put("STATUS", "in_progress");
        
        expect(jdbcTemplate.queryForList(sql)).andReturn(Collections.singletonList(resultMap));
        
        replay(jdbcTemplate);
        
        List<String> stmts = Collections.singletonList(sql);
        executeSQLQueryAction.setStatements(stmts);
        
        Map<String, String> validationElements = new HashMap<String, String>();
        validationElements.put("ORDERTYPE", "small");
        validationElements.put("STATUS", "in_progress");
        
        executeSQLQueryAction.setValidationElements(validationElements);
        
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
        
        Map<String, String> resultMap = new HashMap<String, String>();
        resultMap.put("TYPE", "small");
        resultMap.put("STATE", "in_progress");
        
        expect(jdbcTemplate.queryForList(sql)).andReturn(Collections.singletonList(resultMap));
        
        replay(jdbcTemplate);
        
        List<String> stmts = Collections.singletonList(sql);
        executeSQLQueryAction.setStatements(stmts);
        
        Map<String, String> validationElements = new HashMap<String, String>();
        validationElements.put("TYPE", "small");
        validationElements.put("STATE", "in_progress");
        
        executeSQLQueryAction.setValidationElements(validationElements);
        
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
        
        Map<String, String> resultMap = new HashMap<String, String>();
        resultMap.put("ORDERTYPE", "small");
        resultMap.put("STATUS", "in_progress");
        
        expect(jdbcTemplate.queryForList(sql)).andReturn(Collections.singletonList(resultMap));
        
        replay(jdbcTemplate);
        
        List<String> stmts = Collections.singletonList(sql);
        executeSQLQueryAction.setStatements(stmts);
        
        Map<String, String> validationElements = new HashMap<String, String>();
        validationElements.put("ORDERTYPE", "xxl"); //this is supposed to cause an error
        validationElements.put("STATUS", "in_progress");
        
        executeSQLQueryAction.setValidationElements(validationElements);
        
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
    public void testMultipleStatementsValidationError() {
        String sql1 = "select ORDERTYPE, STATUS from orders where ID=5";
        String sql2 = "select NAME, HEIGHT from customers where ID=1";
        reset(jdbcTemplate);
        
        Map<String, String> resultMap1 = new HashMap<String, String>();
        resultMap1.put("ORDERTYPE", "small");
        resultMap1.put("STATUS", "in_progress");
        
        expect(jdbcTemplate.queryForList(sql1)).andReturn(Collections.singletonList(resultMap1));
        
        Map<String, String> resultMap2 = new HashMap<String, String>();
        resultMap2.put("NAME", "Mickey Mouse");
        resultMap2.put("HEIGHT", "0,3");
        
        expect(jdbcTemplate.queryForList(sql2)).andReturn(Collections.singletonList(resultMap2));
        
        replay(jdbcTemplate);
        
        List<String> stmts = new ArrayList<String>();
        stmts.add(sql1);
        stmts.add(sql2);
        
        executeSQLQueryAction.setStatements(stmts);
        
        Map<String, String> validationElements = new HashMap<String, String>();
        validationElements.put("ORDERTYPE", "small");
        validationElements.put("STATUS", "in_progress");
        validationElements.put("NAME", "Donald Duck"); //this is supposed to cause an error
        validationElements.put("HEIGHT", "0,3");
        
        executeSQLQueryAction.setValidationElements(validationElements);
        
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
}
