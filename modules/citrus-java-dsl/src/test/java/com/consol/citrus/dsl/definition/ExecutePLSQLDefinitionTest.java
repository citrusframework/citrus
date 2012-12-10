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

import static org.easymock.EasyMock.*;

import java.io.*;

import javax.sql.DataSource;

import org.easymock.EasyMock;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.ExecutePLSQLAction;
import com.consol.citrus.dsl.TestNGCitrusTestBuilder;

public class ExecutePLSQLDefinitionTest {
    private DataSource dataSource = EasyMock.createMock(DataSource.class);
    
    private Resource sqlResource = EasyMock.createMock(Resource.class);
    
    @Test
    public void testExecutePLSQLBuilderWithStatement() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                plsql(dataSource)
                    .statement("Test Statement")
                    .statement("Test2 Statement")
                    .statement("Test3 Statement");
            }
        };
          
        builder.configure();
          
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ExecutePLSQLAction.class);
          
        ExecutePLSQLAction action = (ExecutePLSQLAction)builder.getTestCase().getActions().get(0);
        Assert.assertEquals(action.getName(), ExecutePLSQLAction.class.getSimpleName());
        Assert.assertEquals(action.isIgnoreErrors(), false);
        Assert.assertEquals(action.getStatements().toString(), "[Test Statement, Test2 Statement, Test3 Statement]");
        Assert.assertNull(action.getScript());
        Assert.assertNull(action.getSqlResourcePath());
        Assert.assertEquals(action.getDataSource(), dataSource);
    }
    
    @Test
    public void testExecutePLSQLBuilderWithSQLResource() throws IOException {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                plsql(dataSource)
                    .sqlResource(sqlResource);
            }
        };
        
        reset(sqlResource);
        expect(sqlResource.getInputStream()).andReturn(new ByteArrayInputStream("testScript".getBytes())).once();
        replay(sqlResource);
        
        builder.configure();
          
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ExecutePLSQLAction.class);

        ExecutePLSQLAction action = (ExecutePLSQLAction)builder.getTestCase().getActions().get(0);
        Assert.assertEquals(action.getName(), ExecutePLSQLAction.class.getSimpleName());
        Assert.assertEquals(action.isIgnoreErrors(), false);
        Assert.assertEquals(action.getStatements().size(), 0L);
        Assert.assertEquals(action.getScript(), "testScript");
        Assert.assertEquals(action.getDataSource(), dataSource);
    }
    
    @Test
    public void testExecutePLSQLBuilderWithInlineScript() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                plsql(dataSource)
                    .ignoreErrors(true)
                    .sqlScript("testScript");
            }
        };
          
        builder.configure();
          
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ExecutePLSQLAction.class);

        ExecutePLSQLAction action = (ExecutePLSQLAction)builder.getTestCase().getActions().get(0);
        Assert.assertEquals(action.getName(), ExecutePLSQLAction.class.getSimpleName());
        Assert.assertEquals(action.isIgnoreErrors(), true);
        Assert.assertEquals(action.getStatements().size(), 0L);
        Assert.assertNull(action.getSqlResourcePath());
        Assert.assertEquals(action.getScript(), "testScript");
        Assert.assertEquals(action.getDataSource(), dataSource);
    }
}
