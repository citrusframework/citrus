/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.dsl.design;

import com.consol.citrus.TestCase;
import com.consol.citrus.actions.ExecuteSQLQueryAction;
import com.consol.citrus.script.ScriptTypes;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.script.sql.SqlResultSetScriptValidator;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.io.*;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class ExecuteSQLQueryTestDesignerTest extends AbstractTestNGUnitTest {
    private DataSource dataSource = Mockito.mock(DataSource.class);
    private PlatformTransactionManager transactionManager = Mockito.mock(PlatformTransactionManager.class);

    private Resource resource = Mockito.mock(Resource.class);
    private SqlResultSetScriptValidator validator = Mockito.mock(SqlResultSetScriptValidator.class);
    
    @Test
    public void testExecuteSQLQueryWithResource() throws IOException {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                query(dataSource)
                    .sqlResource(resource)
                    .validate("COLUMN", "value")
                    .extract("COLUMN", "variable");
            }
        };
        
        reset(resource);
        when(resource.getFile()).thenReturn(Mockito.mock(File.class));
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("SELECT * FROM DUAL;".getBytes()));

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLQueryAction.class);
        
        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)test.getActions().get(0);
        
        Assert.assertEquals(action.getName(), "sql-query");
        Assert.assertEquals(action.getControlResultSet().size(), 1);
        Assert.assertEquals(action.getControlResultSet().entrySet().iterator().next().toString(), "COLUMN=[value]");
        Assert.assertEquals(action.getExtractVariables().size(), 1);
        Assert.assertEquals(action.getExtractVariables().entrySet().iterator().next().toString(), "COLUMN=variable");
        Assert.assertNull(action.getScriptValidationContext());
        Assert.assertEquals(action.getDataSource(), dataSource);
        Assert.assertEquals(action.getStatements().size(), 1);
        Assert.assertEquals(action.getStatements().get(0), "SELECT * FROM DUAL;");
        Assert.assertNull(action.getSqlResourcePath());
        Assert.assertNull(action.getValidator());

    }
    
    @Test
    public void testExecuteSQLQueryWithStatements() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                query(dataSource)
                    .statement("stmt1")
                    .statement("stmt2")
                    .statement("stmt3")
                    .validate("COLUMN", "value1", "value2")
                    .extract("COLUMN", "variable");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLQueryAction.class);
        
        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)test.getActions().get(0);
        
        Assert.assertEquals(action.getName(), "sql-query");
        Assert.assertEquals(action.getControlResultSet().size(), 1);
        Assert.assertEquals(action.getControlResultSet().entrySet().iterator().next().toString(), "COLUMN=[value1, value2]");
        Assert.assertEquals(action.getExtractVariables().size(), 1);
        Assert.assertEquals(action.getExtractVariables().entrySet().iterator().next().toString(), "COLUMN=variable");
        Assert.assertEquals(action.getStatements().size(), 3);
        Assert.assertEquals(action.getStatements().toString(), "[stmt1, stmt2, stmt3]");
        Assert.assertNull(action.getScriptValidationContext());
        Assert.assertEquals(action.getDataSource(), dataSource);
        Assert.assertNull(action.getValidator());
    }

    @Test
    public void testExecuteSQLQueryWithTransaction() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                query(dataSource)
                    .transactionManager(transactionManager)
                    .transactionTimeout(5000)
                    .transactionIsolationLevel("ISOLATION_READ_COMMITTED")
                    .statement("stmt1")
                    .statement("stmt2")
                    .statement("stmt3")
                    .validate("COLUMN", "value1", "value2")
                    .extract("COLUMN", "variable");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLQueryAction.class);

        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)test.getActions().get(0);

        Assert.assertEquals(action.getName(), "sql-query");
        Assert.assertEquals(action.getControlResultSet().size(), 1);
        Assert.assertEquals(action.getControlResultSet().entrySet().iterator().next().toString(), "COLUMN=[value1, value2]");
        Assert.assertEquals(action.getExtractVariables().size(), 1);
        Assert.assertEquals(action.getExtractVariables().entrySet().iterator().next().toString(), "COLUMN=variable");
        Assert.assertEquals(action.getStatements().size(), 3);
        Assert.assertEquals(action.getStatements().toString(), "[stmt1, stmt2, stmt3]");
        Assert.assertNull(action.getScriptValidationContext());
        Assert.assertEquals(action.getDataSource(), dataSource);
        Assert.assertNull(action.getValidator());
        Assert.assertEquals(action.getTransactionManager(), transactionManager);
        Assert.assertEquals(action.getTransactionTimeout(), "5000");
        Assert.assertEquals(action.getTransactionIsolationLevel(), "ISOLATION_READ_COMMITTED");
    }
    
    @Test
    public void testValidationScript() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                query(dataSource)
                    .statement("stmt")
                    .validateScript("assert rows[0].COLUMN == 'value1'", ScriptTypes.GROOVY);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLQueryAction.class);
        
        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)test.getActions().get(0);
        
        Assert.assertEquals(action.getName(), "sql-query");
        Assert.assertEquals(action.getControlResultSet().size(), 0);
        Assert.assertEquals(action.getExtractVariables().size(), 0);
        Assert.assertNotNull(action.getScriptValidationContext());
        Assert.assertEquals(action.getScriptValidationContext().getValidationScript(), "assert rows[0].COLUMN == 'value1'");
        Assert.assertNull(action.getScriptValidationContext().getValidationScriptResourcePath());
        Assert.assertEquals(action.getStatements().size(), 1);
        Assert.assertEquals(action.getStatements().toString(), "[stmt]");
        Assert.assertEquals(action.getDataSource(), dataSource);
    }
    
    @Test
    public void testValidationScriptResource() throws IOException {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                query(dataSource)
                    .statement("stmt")
                    .validateScript(resource, ScriptTypes.GROOVY);
            }
        };
        
        reset(resource);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("someScript".getBytes()));
        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLQueryAction.class);
        
        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)test.getActions().get(0);
        
        Assert.assertEquals(action.getName(), "sql-query");
        Assert.assertEquals(action.getControlResultSet().size(), 0);
        Assert.assertEquals(action.getExtractVariables().size(), 0);
        Assert.assertNotNull(action.getScriptValidationContext());
        Assert.assertEquals(action.getScriptValidationContext().getValidationScript(), "someScript");
        Assert.assertNull(action.getScriptValidationContext().getValidationScriptResourcePath());
        Assert.assertEquals(action.getStatements().size(), 1);
        Assert.assertEquals(action.getStatements().toString(), "[stmt]");
        Assert.assertEquals(action.getDataSource(), dataSource);

    }
    
    @Test
    public void testGroovyValidationScript() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                query(dataSource)
                    .statement("stmt")
                    .groovy("assert rows[0].COLUMN == 'value1'");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLQueryAction.class);
        
        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)test.getActions().get(0);
        
        Assert.assertEquals(action.getName(), "sql-query");
        Assert.assertEquals(action.getControlResultSet().size(), 0);
        Assert.assertEquals(action.getExtractVariables().size(), 0);
        Assert.assertNotNull(action.getScriptValidationContext());
        Assert.assertEquals(action.getScriptValidationContext().getValidationScript(), "assert rows[0].COLUMN == 'value1'");
        Assert.assertNull(action.getScriptValidationContext().getValidationScriptResourcePath());
        Assert.assertEquals(action.getStatements().size(), 1);
        Assert.assertEquals(action.getStatements().toString(), "[stmt]");
        Assert.assertEquals(action.getDataSource(), dataSource);
    }
    
    @Test
    public void testGroovyValidationScriptResource() throws IOException {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                query(dataSource)
                    .statement("stmt")
                    .groovy(resource);
            }
        };
        
        reset(resource);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("someScript".getBytes()));
        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLQueryAction.class);
        
        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)test.getActions().get(0);
        
        Assert.assertEquals(action.getName(), "sql-query");
        Assert.assertEquals(action.getControlResultSet().size(), 0);
        Assert.assertEquals(action.getExtractVariables().size(), 0);
        Assert.assertNotNull(action.getScriptValidationContext());
        Assert.assertEquals(action.getScriptValidationContext().getValidationScript(), "someScript");
        Assert.assertNull(action.getScriptValidationContext().getValidationScriptResourcePath());
        Assert.assertEquals(action.getStatements().size(), 1);
        Assert.assertEquals(action.getStatements().toString(), "[stmt]");
        Assert.assertEquals(action.getDataSource(), dataSource);

    }

    @Test
    public void testCustomScriptValidator() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                query(dataSource)
                        .statement("stmt")
                        .validateScript("assert something", ScriptTypes.GROOVY)
                        .validator(validator);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLQueryAction.class);

        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)test.getActions().get(0);

        Assert.assertEquals(action.getName(), "sql-query");
        Assert.assertEquals(action.getControlResultSet().size(), 0);
        Assert.assertEquals(action.getExtractVariables().size(), 0);
        Assert.assertNotNull(action.getScriptValidationContext());
        Assert.assertEquals(action.getScriptValidationContext().getValidationScript(), "assert something");
        Assert.assertNull(action.getScriptValidationContext().getValidationScriptResourcePath());
        Assert.assertEquals(action.getStatements().size(), 1);
        Assert.assertEquals(action.getStatements().toString(), "[stmt]");
        Assert.assertEquals(action.getDataSource(), dataSource);
        Assert.assertEquals(action.getValidator(), validator);
    }
}
