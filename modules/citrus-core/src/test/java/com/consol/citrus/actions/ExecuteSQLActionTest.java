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
