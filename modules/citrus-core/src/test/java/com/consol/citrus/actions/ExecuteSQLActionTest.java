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

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;


/**
 * @author Christoph Deppisch
 */
public class ExecuteSQLActionTest extends AbstractTestNGUnitTest {

    private static final String DB_STMT_1 = "DELETE * FROM ERRORS WHERE STATUS='resolved'";
    private static final String DB_STMT_2 = "DELETE * FROM CONFIGURATION WHERE VERSION=1";

    private ExecuteSQLAction executeSQLAction;
    
    private JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
    private PlatformTransactionManager transactionManager = Mockito.mock(PlatformTransactionManager.class);

    @BeforeMethod
    public void setUp() {
        executeSQLAction  = new ExecuteSQLAction();
        executeSQLAction.setJdbcTemplate(jdbcTemplate);
    }
    
	@Test
	public void testSQLExecutionWithInlineStatements() {
	    List<String> stmts = new ArrayList<>();
	    stmts.add(DB_STMT_1);
	    stmts.add(DB_STMT_2);
	    
	    executeSQLAction.setStatements(stmts);
	    
	    reset(jdbcTemplate);
	    
	    executeSQLAction.execute(context);

	    verify(jdbcTemplate).execute(DB_STMT_1);
	    verify(jdbcTemplate).execute(DB_STMT_2);
	}
	
	@Test
	public void testSQLExecutionWithTransactions() {
	    List<String> stmts = new ArrayList<>();
	    stmts.add(DB_STMT_1);
	    stmts.add(DB_STMT_2);

	    executeSQLAction.setStatements(stmts);
	    executeSQLAction.setTransactionManager(transactionManager);

	    reset(jdbcTemplate, transactionManager);

	    executeSQLAction.execute(context);

	    verify(jdbcTemplate).execute(DB_STMT_1);
	    verify(jdbcTemplate).execute(DB_STMT_2);
	}
	
	@Test
    public void testSQLExecutionWithFileResource() {
        executeSQLAction.setSqlResourcePath("classpath:com/consol/citrus/actions/test-sql-statements.sql");
        
        reset(jdbcTemplate);
        
        executeSQLAction.execute(context);

        verify(jdbcTemplate).execute(DB_STMT_1);
        verify(jdbcTemplate).execute(DB_STMT_2);
    }
	
	@Test
    public void testSQLExecutionWithInlineScriptVariableSupport() {
	    context.setVariable("resolvedStatus", "resolved");
	    context.setVariable("version", "1");
	    
	    List<String> stmts = new ArrayList<>();
        stmts.add("DELETE * FROM ERRORS WHERE STATUS='${resolvedStatus}'");
        stmts.add("DELETE * FROM CONFIGURATION WHERE VERSION=${version}");
        
        executeSQLAction.setStatements(stmts);
        
        reset(jdbcTemplate);
        
        executeSQLAction.execute(context);

        verify(jdbcTemplate).execute(DB_STMT_1);
        verify(jdbcTemplate).execute(DB_STMT_2);
    }
	
	@Test
    public void testSQLExecutionWithFileResourceVariableSupport() {
	    context.setVariable("resolvedStatus", "resolved");
        context.setVariable("version", "1");
        
        executeSQLAction.setSqlResourcePath("classpath:com/consol/citrus/actions/test-sql-with-variables.sql");
        
        reset(jdbcTemplate);

        executeSQLAction.execute(context);

        verify(jdbcTemplate).execute(DB_STMT_1);
        verify(jdbcTemplate).execute(DB_STMT_2);
    }
	
    @Test
    @SuppressWarnings("serial")
    public void testSQLExecutionIgnoreErrors() {
        List<String> stmts = new ArrayList<>();
        stmts.add(DB_STMT_1);
        stmts.add(DB_STMT_2);
        
        executeSQLAction.setStatements(stmts);
        
        executeSQLAction.setIgnoreErrors(true);
        
        reset(jdbcTemplate);
        
        doThrow(new DataAccessException("Something went wrong!") {}).when(jdbcTemplate).execute(DB_STMT_2);

        executeSQLAction.execute(context);
        verify(jdbcTemplate).execute(DB_STMT_1);
    }
	
    @Test(expectedExceptions = CitrusRuntimeException.class)
    @SuppressWarnings("serial")
    public void testSQLExecutionErrorForwarding() {
        List<String> stmts = new ArrayList<>();
        stmts.add(DB_STMT_1);
        stmts.add(DB_STMT_2);
        
        executeSQLAction.setStatements(stmts);
        
        executeSQLAction.setIgnoreErrors(false);
        
        reset(jdbcTemplate);
        
        doThrow(new DataAccessException("Something went wrong!") {}).when(jdbcTemplate).execute(DB_STMT_2);

        executeSQLAction.execute(context);
        verify(jdbcTemplate).execute(DB_STMT_1);
    }
}
