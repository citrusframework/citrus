/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.actions;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.*;

import java.util.ArrayList;
import java.util.List;

import org.easymock.classextension.EasyMock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractBaseTest;

/**
 * @author Christoph Deppisch
 */
public class ExecuteSQLActionTest extends AbstractBaseTest {
	
    private ExecuteSQLAction executeSQLAction;
    
    private JdbcTemplate jdbcTemplate = EasyMock.createMock(JdbcTemplate.class);
    
    @BeforeMethod
    public void setUp() {
        executeSQLAction  = new ExecuteSQLAction();
        executeSQLAction.setJdbcTemplate(jdbcTemplate);
    }
    
	@Test
	public void testSQLExecutionWithInlineStatements() {
	    List<String> stmts = new ArrayList<String>();
	    stmts.add("DELETE * FROM ERRORS WHERE STATUS='resolved'");
	    stmts.add("DELETE * FROM CONFIGURATION WHERE VERSION=1");
	    
	    executeSQLAction.setStatements(stmts);
	    
	    reset(jdbcTemplate);
	    
	    jdbcTemplate.execute("DELETE * FROM ERRORS WHERE STATUS='resolved'");
	    expectLastCall().once();
	    jdbcTemplate.execute("DELETE * FROM CONFIGURATION WHERE VERSION=1");
        expectLastCall().once();
        
	    replay(jdbcTemplate);
	    
	    executeSQLAction.execute(context);
	    
	    verify(jdbcTemplate);
	}
	
	@Test
    public void testSQLExecutionWithFileResource() {
        executeSQLAction.setSqlResource(new ClassPathResource("test-sql-statements.sql", ExecuteSQLActionTest.class));
        
        reset(jdbcTemplate);
        
        jdbcTemplate.execute("DELETE * FROM ERRORS WHERE STATUS='resolved'");
        expectLastCall().once();
        jdbcTemplate.execute("DELETE * FROM CONFIGURATION WHERE VERSION=1");
        expectLastCall().once();
        
        replay(jdbcTemplate);
        
        executeSQLAction.execute(context);
        
        verify(jdbcTemplate);
    }
	
	@Test
    public void testSQLExecutionWithInlineScriptVariableSupport() {
	    context.setVariable("resolvedStatus", "resolved");
	    context.setVariable("version", "1");
	    
	    List<String> stmts = new ArrayList<String>();
        stmts.add("DELETE * FROM ERRORS WHERE STATUS='${resolvedStatus}'");
        stmts.add("DELETE * FROM CONFIGURATION WHERE VERSION=${version}");
        
        executeSQLAction.setStatements(stmts);
        
        reset(jdbcTemplate);
        
        jdbcTemplate.execute("DELETE * FROM ERRORS WHERE STATUS='resolved'");
        expectLastCall().once();
        jdbcTemplate.execute("DELETE * FROM CONFIGURATION WHERE VERSION=1");
        expectLastCall().once();
        
        replay(jdbcTemplate);
        
        executeSQLAction.execute(context);
        
        verify(jdbcTemplate);
    }
	
	@Test
    public void testSQLExecutionWithFileResourceVariableSupport() {
	    context.setVariable("resolvedStatus", "resolved");
        context.setVariable("version", "1");
        
        executeSQLAction.setSqlResource(new ClassPathResource("test-sql-with-variables.sql", ExecuteSQLActionTest.class));
        
        reset(jdbcTemplate);
        
        jdbcTemplate.execute("DELETE * FROM ERRORS WHERE STATUS='resolved'");
        expectLastCall().once();
        jdbcTemplate.execute("DELETE * FROM CONFIGURATION WHERE VERSION=1");
        expectLastCall().once();
        
        replay(jdbcTemplate);
        
        executeSQLAction.execute(context);
        
        verify(jdbcTemplate);
    }
	
    @Test
    @SuppressWarnings("serial")
    public void testSQLExecutionIgnoreErrors() {
        List<String> stmts = new ArrayList<String>();
        stmts.add("DELETE * FROM ERRORS WHERE STATUS='resolved'");
        stmts.add("DELETE * FROM CONFIGURATION WHERE VERSION=1");
        
        executeSQLAction.setStatements(stmts);
        
        executeSQLAction.setIgnoreErrors(true);
        
        reset(jdbcTemplate);
        
        jdbcTemplate.execute("DELETE * FROM ERRORS WHERE STATUS='resolved'");
        expectLastCall().once();
        jdbcTemplate.execute("DELETE * FROM CONFIGURATION WHERE VERSION=1");
        expectLastCall().andThrow(new DataAccessException("Something went wrong!") {}).once();
        
        replay(jdbcTemplate);
        
        executeSQLAction.execute(context);
        
        verify(jdbcTemplate);
    }
	
    @Test(expectedExceptions = CitrusRuntimeException.class)
    @SuppressWarnings("serial")
    public void testSQLExecutionErrorForwarding() {
        List<String> stmts = new ArrayList<String>();
        stmts.add("DELETE * FROM ERRORS WHERE STATUS='resolved'");
        stmts.add("DELETE * FROM CONFIGURATION WHERE VERSION=1");
        
        executeSQLAction.setStatements(stmts);
        
        executeSQLAction.setIgnoreErrors(false);
        
        reset(jdbcTemplate);
        
        jdbcTemplate.execute("DELETE * FROM ERRORS WHERE STATUS='resolved'");
        expectLastCall().once();
        jdbcTemplate.execute("DELETE * FROM CONFIGURATION WHERE VERSION=1");
        expectLastCall().andThrow(new DataAccessException("Something went wrong!") {}).once();
        
        replay(jdbcTemplate);
        
        executeSQLAction.execute(context);
    }
}
