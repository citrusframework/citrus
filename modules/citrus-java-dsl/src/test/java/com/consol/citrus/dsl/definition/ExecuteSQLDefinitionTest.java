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

import java.io.File;
import java.io.IOException;

import javax.sql.DataSource;

import org.easymock.EasyMock;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.ExecuteSQLAction;


public class ExecuteSQLDefinitionTest {    
    private DataSource dataSource = EasyMock.createMock(DataSource.class);
    
    private Resource resource = EasyMock.createMock(Resource.class);
    private File file = EasyMock.createMock(File.class);
    
    @Test
    public void TestExecuteSQLBuilderWithStatement() {
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                sql(dataSource)
                    .statement("Test Statement")
                    .statement("Test2 Statement")
                    .statement("Test3 Statement")
                    .ignoreErrors(false);
            }
        };
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), ExecuteSQLAction.class);
        
        ExecuteSQLAction action = (ExecuteSQLAction)builder.testCase().getActions().get(0);
        Assert.assertEquals(action.getName(), ExecuteSQLAction.class.getSimpleName());
        Assert.assertEquals(action.getStatements().toString(), "[Test Statement, Test2 Statement, Test3 Statement]");
        Assert.assertEquals(action.isIgnoreErrors(), false);
        Assert.assertEquals(action.getDataSource(), dataSource);
    }
    
    @Test
    public void TestExecuteSQLBuilderWithSQLResource() throws IOException {
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                sql(dataSource)
                    .sqlResource(resource)
                    .ignoreErrors(true);
            }
        };
    
        reset(resource, file);
        expect(resource.getFile()).andReturn(file).once();
        expect(file.getAbsolutePath()).andReturn("classpath:some.file").once();
        replay(resource, file);
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), ExecuteSQLAction.class);

        ExecuteSQLAction action = (ExecuteSQLAction)builder.testCase().getActions().get(0);
        Assert.assertEquals(action.getName(), ExecuteSQLAction.class.getSimpleName());
        Assert.assertEquals(action.isIgnoreErrors(), true);
        Assert.assertEquals(action.getDataSource(), dataSource);
        Assert.assertEquals(action.getSqlResourcePath(), "classpath:some.file");
        
        verify(resource, file);
    }
}
