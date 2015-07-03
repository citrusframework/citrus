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

package com.consol.citrus.dsl.definition;

import com.consol.citrus.TestCase;
import com.consol.citrus.actions.ExecuteSQLQueryAction;
import com.consol.citrus.script.ScriptTypes;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.script.sql.SqlResultSetScriptValidator;
import org.easymock.EasyMock;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import static org.easymock.EasyMock.*;

public class ExecuteSQLQueryDefinitionTest extends AbstractTestNGUnitTest {
    private DataSource dataSource = EasyMock.createMock(DataSource.class);
    
    private Resource resource = EasyMock.createMock(Resource.class);
    private File file = EasyMock.createMock(File.class);
    
    private SqlResultSetScriptValidator validator = EasyMock.createMock(SqlResultSetScriptValidator.class);
    
    @Test
    public void testExecuteSQLQueryWithResource() throws IOException {
        MockDesigner builder = new MockDesigner(applicationContext) {
            @Override
            public void configure() {
                query(dataSource)
                    .sqlResource(resource)
                    .validate("COLUMN", "value")
                    .extract("COLUMN", "variable")
                    .validator(validator);
            }
        };
        
        reset(resource, file);
        expect(resource.getFile()).andReturn(file).once();
        expect(file.getAbsolutePath()).andReturn("classpath:some.file").once();
        replay(resource, file);

        builder.execute();

        TestCase test = builder.build();
        Assert.assertEquals(test.getActions().size(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLQueryAction.class);
        
        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)test.getActions().get(0);
        
        Assert.assertEquals(action.getName(), "sql-query");
        Assert.assertEquals(action.getControlResultSet().size(), 1);
        Assert.assertEquals(action.getControlResultSet().entrySet().iterator().next().toString(), "COLUMN=[value]");
        Assert.assertEquals(action.getExtractVariables().size(), 1);
        Assert.assertEquals(action.getExtractVariables().entrySet().iterator().next().toString(), "COLUMN=variable");
        Assert.assertNull(action.getScriptValidationContext());
        Assert.assertEquals(action.getDataSource(), dataSource);
        Assert.assertEquals(action.getSqlResourcePath(), "classpath:some.file");
        Assert.assertEquals(action.getValidator(), validator);
        
        verify(resource, file);
    }
    
    @Test
    public void testExecuteSQLQueryWithStatements() {
        MockDesigner builder = new MockDesigner(applicationContext) {
        @Override
        public void configure() {
            query(dataSource)
                .statement("stmt1")
                .statement("stmt2")
                .statement("stmt3")
                .validate("COLUMN", "value1", "value2")
                .extract("COLUMN", "variable")
                .validator(validator);
            }
        };

        builder.execute();

        TestCase test = builder.build();
        Assert.assertEquals(test.getActions().size(), 1);
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
        Assert.assertEquals(action.getValidator(), validator);
    }
    
    @Test
    public void testValidationScript() {
        MockDesigner builder = new MockDesigner(applicationContext) {
        @Override
        public void configure() {
            query(dataSource)
                .statement("stmt")
                .validateScript("assert row[0].COLUMN == 'value1'", ScriptTypes.GROOVY);
            }
        };

        builder.execute();

        TestCase test = builder.build();
        Assert.assertEquals(test.getActions().size(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLQueryAction.class);
        
        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)test.getActions().get(0);
        
        Assert.assertEquals(action.getName(), "sql-query");
        Assert.assertEquals(action.getControlResultSet().size(), 0);
        Assert.assertEquals(action.getExtractVariables().size(), 0);
        Assert.assertNotNull(action.getScriptValidationContext());
        Assert.assertEquals(action.getScriptValidationContext().getValidationScript(), "assert row[0].COLUMN == 'value1'");
        Assert.assertNull(action.getScriptValidationContext().getValidationScriptResourcePath());
        Assert.assertEquals(action.getStatements().size(), 1);
        Assert.assertEquals(action.getStatements().toString(), "[stmt]");
        Assert.assertEquals(action.getDataSource(), dataSource);
    }
    
    @Test
    public void testValidationScriptResource() throws IOException {
        MockDesigner builder = new MockDesigner(applicationContext) {
        @Override
        public void configure() {
            query(dataSource)
                .statement("stmt")
                .validateScript(resource, ScriptTypes.GROOVY);
            }
        };
        
        reset(resource, file);
        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("someScript".getBytes())).once();
        replay(resource, file);

        builder.execute();

        TestCase test = builder.build();
        Assert.assertEquals(test.getActions().size(), 1);
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
        
        verify(resource, file);
    }
    
    @Test
    public void testGroovyValidationScript() {
        MockDesigner builder = new MockDesigner(applicationContext) {
        @Override
        public void configure() {
            query(dataSource)
                .statement("stmt")
                .groovy("assert row[0].COLUMN == 'value1'");
            }
        };

        builder.execute();

        TestCase test = builder.build();
        Assert.assertEquals(test.getActions().size(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLQueryAction.class);
        
        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)test.getActions().get(0);
        
        Assert.assertEquals(action.getName(), "sql-query");
        Assert.assertEquals(action.getControlResultSet().size(), 0);
        Assert.assertEquals(action.getExtractVariables().size(), 0);
        Assert.assertNotNull(action.getScriptValidationContext());
        Assert.assertEquals(action.getScriptValidationContext().getValidationScript(), "assert row[0].COLUMN == 'value1'");
        Assert.assertNull(action.getScriptValidationContext().getValidationScriptResourcePath());
        Assert.assertEquals(action.getStatements().size(), 1);
        Assert.assertEquals(action.getStatements().toString(), "[stmt]");
        Assert.assertEquals(action.getDataSource(), dataSource);
    }
    
    @Test
    public void testGroovyValidationScriptResource() throws IOException {
        MockDesigner builder = new MockDesigner(applicationContext) {
        @Override
        public void configure() {
            query(dataSource)
                .statement("stmt")
                .groovy(resource);
            }
        };
        
        reset(resource, file);
        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("someScript".getBytes())).once();
        replay(resource, file);

        builder.execute();

        TestCase test = builder.build();
        Assert.assertEquals(test.getActions().size(), 1);
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
        
        verify(resource, file);
    }
}
