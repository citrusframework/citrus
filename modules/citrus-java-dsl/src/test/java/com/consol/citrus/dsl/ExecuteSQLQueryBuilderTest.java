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

package com.consol.citrus.dsl;

import javax.sql.DataSource;

import org.easymock.EasyMock;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.ExecuteSQLQueryAction;
import com.consol.citrus.script.ScriptTypes;
import com.consol.citrus.validation.script.sql.SqlResultSetScriptValidator;

public class ExecuteSQLQueryBuilderTest {   
    private DataSource dataSource = EasyMock.createMock(DataSource.class);
    
    private Resource resource = EasyMock.createMock(Resource.class);
    
    private SqlResultSetScriptValidator validator = EasyMock.createMock(SqlResultSetScriptValidator.class);
    
    @Test
    public void testExecuteSQLQueryWithResource() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            protected void configure() {
                query(dataSource)
                    .sqlResource(resource)
                    .validate("COLUMN", "value")
                    .extract("COLUMN", "variable")
                    .validator(validator);
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ExecuteSQLQueryAction.class);
        
        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)builder.getTestCase().getActions().get(0);
        
        Assert.assertEquals(action.getName(), ExecuteSQLQueryAction.class.getSimpleName());
        Assert.assertEquals(action.getControlResultSet().size(), 1);
        Assert.assertEquals(action.getControlResultSet().entrySet().iterator().next().toString(), "COLUMN=[value]");
        Assert.assertEquals(action.getExtractVariables().size(), 1);
        Assert.assertEquals(action.getExtractVariables().entrySet().iterator().next().toString(), "COLUMN=variable");
        Assert.assertNull(action.getScriptValidationContext());
        Assert.assertEquals(action.getDataSource(), dataSource);
        Assert.assertEquals(action.getSqlResource(), resource);
        Assert.assertEquals(action.getValidator(), validator);
    }
    
    @Test
    public void testExecuteSQLQueryWithStatements() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
        @Override
        protected void configure() {
            query(dataSource)
                .statement("stmt1")
                .statement("stmt2")
                .statement("stmt3")
                .validate("COLUMN", "value1", "value2")
                .extract("COLUMN", "variable")
                .validator(validator);
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ExecuteSQLQueryAction.class);
        
        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)builder.getTestCase().getActions().get(0);
        
        Assert.assertEquals(action.getName(), ExecuteSQLQueryAction.class.getSimpleName());
        Assert.assertEquals(action.getControlResultSet().size(), 1);
        Assert.assertEquals(action.getControlResultSet().entrySet().iterator().next().toString(), "COLUMN=[value1, value2]");
        Assert.assertEquals(action.getExtractVariables().size(), 1);
        Assert.assertEquals(action.getExtractVariables().entrySet().iterator().next().toString(), "COLUMN=variable");
        Assert.assertEquals(action.getStatements().size(), 3);
        Assert.assertEquals(action.getStatements().toString(), "[stmt1, stmt2, stmt3]");
        Assert.assertNull(action.getScriptValidationContext());
        Assert.assertEquals(action.getDataSource(), dataSource);
        Assert.assertEquals(action.getValidator(), validator);
    }
    
    @Test
    public void testValidationScript() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
        @Override
        protected void configure() {
            query(dataSource)
                .statement("stmt")
                .validateScript("assert row[0].COLUMN == 'value1'", ScriptTypes.GROOVY);
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ExecuteSQLQueryAction.class);
        
        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)builder.getTestCase().getActions().get(0);
        
        Assert.assertEquals(action.getName(), ExecuteSQLQueryAction.class.getSimpleName());
        Assert.assertEquals(action.getControlResultSet().size(), 0);
        Assert.assertEquals(action.getExtractVariables().size(), 0);
        Assert.assertNotNull(action.getScriptValidationContext());
        Assert.assertEquals(action.getScriptValidationContext().getValidationScript(), "assert row[0].COLUMN == 'value1'");
        Assert.assertNull(action.getScriptValidationContext().getValidationScriptResource());
        Assert.assertEquals(action.getStatements().size(), 1);
        Assert.assertEquals(action.getStatements().toString(), "[stmt]");
        Assert.assertEquals(action.getDataSource(), dataSource);
    }
    
    @Test
    public void testValidationScriptResource() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
        @Override
        protected void configure() {
            query(dataSource)
                .statement("stmt")
                .validateScript(resource, ScriptTypes.GROOVY);
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ExecuteSQLQueryAction.class);
        
        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)builder.getTestCase().getActions().get(0);
        
        Assert.assertEquals(action.getName(), ExecuteSQLQueryAction.class.getSimpleName());
        Assert.assertEquals(action.getControlResultSet().size(), 0);
        Assert.assertEquals(action.getExtractVariables().size(), 0);
        Assert.assertNotNull(action.getScriptValidationContext());
        Assert.assertEquals(action.getScriptValidationContext().getValidationScript(), "");
        Assert.assertNotNull(action.getScriptValidationContext().getValidationScriptResource());
        Assert.assertEquals(action.getStatements().size(), 1);
        Assert.assertEquals(action.getStatements().toString(), "[stmt]");
        Assert.assertEquals(action.getDataSource(), dataSource);
    }
    
    @Test
    public void testGroovyValidationScript() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
        @Override
        protected void configure() {
            query(dataSource)
                .statement("stmt")
                .groovy("assert row[0].COLUMN == 'value1'");
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ExecuteSQLQueryAction.class);
        
        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)builder.getTestCase().getActions().get(0);
        
        Assert.assertEquals(action.getName(), ExecuteSQLQueryAction.class.getSimpleName());
        Assert.assertEquals(action.getControlResultSet().size(), 0);
        Assert.assertEquals(action.getExtractVariables().size(), 0);
        Assert.assertNotNull(action.getScriptValidationContext());
        Assert.assertEquals(action.getScriptValidationContext().getValidationScript(), "assert row[0].COLUMN == 'value1'");
        Assert.assertNull(action.getScriptValidationContext().getValidationScriptResource());
        Assert.assertEquals(action.getStatements().size(), 1);
        Assert.assertEquals(action.getStatements().toString(), "[stmt]");
        Assert.assertEquals(action.getDataSource(), dataSource);
    }
    
    @Test
    public void testGroovyValidationScriptResource() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
        @Override
        protected void configure() {
            query(dataSource)
                .statement("stmt")
                .groovy(resource);
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ExecuteSQLQueryAction.class);
        
        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)builder.getTestCase().getActions().get(0);
        
        Assert.assertEquals(action.getName(), ExecuteSQLQueryAction.class.getSimpleName());
        Assert.assertEquals(action.getControlResultSet().size(), 0);
        Assert.assertEquals(action.getExtractVariables().size(), 0);
        Assert.assertNotNull(action.getScriptValidationContext());
        Assert.assertEquals(action.getScriptValidationContext().getValidationScript(), "");
        Assert.assertNotNull(action.getScriptValidationContext().getValidationScriptResource());
        Assert.assertEquals(action.getStatements().size(), 1);
        Assert.assertEquals(action.getStatements().toString(), "[stmt]");
        Assert.assertEquals(action.getDataSource(), dataSource);
    }
}
