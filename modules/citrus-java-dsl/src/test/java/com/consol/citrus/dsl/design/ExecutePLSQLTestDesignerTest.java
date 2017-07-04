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
import com.consol.citrus.actions.ExecutePLSQLAction;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 1.3
 */
public class ExecutePLSQLTestDesignerTest extends AbstractTestNGUnitTest {
    private DataSource dataSource = Mockito.mock(DataSource.class);
    private Resource sqlResource = Mockito.mock(Resource.class);

    private PlatformTransactionManager transactionManager = Mockito.mock(PlatformTransactionManager.class);

    @Test
    public void testExecutePLSQLBuilderWithStatement() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                plsql(dataSource)
                    .statement("TEST_STMT_1")
                    .statement("TEST_STMT_2")
                    .statement("TEST_STMT_3");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecutePLSQLAction.class);
          
        ExecutePLSQLAction action = (ExecutePLSQLAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "plsql");
        Assert.assertEquals(action.isIgnoreErrors(), false);
        Assert.assertEquals(action.getStatements().toString(), "[TEST_STMT_1, TEST_STMT_2, TEST_STMT_3]");
        Assert.assertNull(action.getScript());
        Assert.assertNull(action.getSqlResourcePath());
        Assert.assertEquals(action.getDataSource(), dataSource);
    }

    @Test
    public void testExecutePLSQLBuilderWithTransaction() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                plsql(dataSource)
                    .transactionManager(transactionManager)
                    .transactionTimeout(5000)
                    .transactionIsolationLevel("ISOLATION_READ_COMMITTED")
                    .statement("TEST_STMT_1")
                    .statement("TEST_STMT_2")
                    .statement("TEST_STMT_3");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecutePLSQLAction.class);

        ExecutePLSQLAction action = (ExecutePLSQLAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "plsql");
        Assert.assertEquals(action.isIgnoreErrors(), false);
        Assert.assertEquals(action.getStatements().toString(), "[TEST_STMT_1, TEST_STMT_2, TEST_STMT_3]");
        Assert.assertNull(action.getScript());
        Assert.assertNull(action.getSqlResourcePath());
        Assert.assertEquals(action.getDataSource(), dataSource);
        Assert.assertEquals(action.getTransactionManager(), transactionManager);
        Assert.assertEquals(action.getTransactionTimeout(), "5000");
        Assert.assertEquals(action.getTransactionIsolationLevel(), "ISOLATION_READ_COMMITTED");
    }
    
    @Test
    public void testExecutePLSQLBuilderWithSQLResource() throws IOException {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                plsql(dataSource)
                    .sqlResource(sqlResource);
            }
        };
        
        reset(sqlResource);
        when(sqlResource.getInputStream()).thenReturn(new ByteArrayInputStream("testScript".getBytes()));
        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecutePLSQLAction.class);

        ExecutePLSQLAction action = (ExecutePLSQLAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "plsql");
        Assert.assertEquals(action.isIgnoreErrors(), false);
        Assert.assertEquals(action.getStatements().size(), 0L);
        Assert.assertEquals(action.getScript(), "testScript");
        Assert.assertEquals(action.getDataSource(), dataSource);
    }
    
    @Test
    public void testExecutePLSQLBuilderWithInlineScript() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                plsql(dataSource)
                    .ignoreErrors(true)
                    .sqlScript("testScript");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecutePLSQLAction.class);

        ExecutePLSQLAction action = (ExecutePLSQLAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "plsql");
        Assert.assertEquals(action.isIgnoreErrors(), true);
        Assert.assertEquals(action.getStatements().size(), 0L);
        Assert.assertNull(action.getSqlResourcePath());
        Assert.assertEquals(action.getScript(), "testScript");
        Assert.assertEquals(action.getDataSource(), dataSource);
    }
}
