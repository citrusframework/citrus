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
import com.consol.citrus.actions.ExecuteSQLAction;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.io.*;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 * @since 1.3
 */
public class ExecuteSQLTestDesignerTest extends AbstractTestNGUnitTest {
    private DataSource dataSource = Mockito.mock(DataSource.class);
    
    private Resource resource = Mockito.mock(Resource.class);
    private File file = Mockito.mock(File.class);
    
    @Test
    public void testExecuteSQLBuilderWithStatement() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                sql(dataSource)
                    .statement("TEST_STMT_1")
                    .statement("TEST_STMT_2")
                    .statement("TEST_STMT_3")
                    .ignoreErrors(false);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLAction.class);
        
        ExecuteSQLAction action = (ExecuteSQLAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "sql");
        Assert.assertEquals(action.getStatements().toString(), "[TEST_STMT_1, TEST_STMT_2, TEST_STMT_3]");
        Assert.assertEquals(action.isIgnoreErrors(), false);
        Assert.assertEquals(action.getDataSource(), dataSource);
    }

    @Test
    public void testExecuteSQLBuilderWithResource() throws IOException {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                sql(dataSource)
                    .sqlResource(resource)
                    .ignoreErrors(true);
            }
        };

        reset(resource, file);
        when(resource.getFile()).thenReturn(file);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("SELECT * FROM DUAL;".getBytes()));
        when(file.getAbsolutePath()).thenReturn("classpath:some.file");
        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLAction.class);

        ExecuteSQLAction action = (ExecuteSQLAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "sql");
        Assert.assertEquals(action.isIgnoreErrors(), true);
        Assert.assertEquals(action.getDataSource(), dataSource);
        Assert.assertEquals(action.getStatements().toString(), "[SELECT * FROM DUAL;]");
        Assert.assertNull(action.getSqlResourcePath());

    }
}
