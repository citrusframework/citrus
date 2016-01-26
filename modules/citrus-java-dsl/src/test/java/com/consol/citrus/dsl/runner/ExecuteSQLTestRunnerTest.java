/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.dsl.runner;

import com.consol.citrus.TestCase;
import com.consol.citrus.actions.ExecuteSQLAction;
import com.consol.citrus.dsl.builder.BuilderSupport;
import com.consol.citrus.dsl.builder.ExecuteSQLBuilder;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class ExecuteSQLTestRunnerTest extends AbstractTestNGUnitTest {
    private JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
    private Resource resource = Mockito.mock(Resource.class);
    private File file = Mockito.mock(File.class);
    
    @Test
    public void testExecuteSQLBuilderWithStatement() {
        reset(jdbcTemplate);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                sql(new BuilderSupport<ExecuteSQLBuilder>() {
                    @Override
                    public void configure(ExecuteSQLBuilder builder) {
                        builder.jdbcTemplate(jdbcTemplate)
                                .statement("TEST_STMT_1")
                                .statement("TEST_STMT_2")
                                .statement("TEST_STMT_3")
                                .ignoreErrors(false);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLAction.class);
        Assert.assertEquals(test.getLastExecutedAction().getClass(), ExecuteSQLAction.class);

        ExecuteSQLAction action = (ExecuteSQLAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "sql");
        Assert.assertEquals(action.getStatements().toString(), "[TEST_STMT_1, TEST_STMT_2, TEST_STMT_3]");
        Assert.assertEquals(action.isIgnoreErrors(), false);
        Assert.assertEquals(action.getJdbcTemplate(), jdbcTemplate);

        verify(jdbcTemplate).execute("TEST_STMT_1");
        verify(jdbcTemplate).execute("TEST_STMT_2");
        verify(jdbcTemplate).execute("TEST_STMT_3");
    }

    @Test
    public void testExecuteSQLBuilderWithResource() throws IOException {
        reset(jdbcTemplate);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                sql(new BuilderSupport<ExecuteSQLBuilder>() {
                    @Override
                    public void configure(ExecuteSQLBuilder builder) {
                        builder.jdbcTemplate(jdbcTemplate)
                                .sqlResource(new ClassPathResource("com/consol/citrus/dsl/runner/script.sql"))
                                .ignoreErrors(true);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLAction.class);
        Assert.assertEquals(test.getLastExecutedAction().getClass(), ExecuteSQLAction.class);

        ExecuteSQLAction action = (ExecuteSQLAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "sql");
        Assert.assertEquals(action.isIgnoreErrors(), true);
        Assert.assertEquals(action.getJdbcTemplate(), jdbcTemplate);
        Assert.assertEquals(action.getStatements().size(), 3);
        Assert.assertNull(action.getSqlResourcePath());

        verify(jdbcTemplate).execute("TEST_STMT_1");
        verify(jdbcTemplate).execute("TEST_STMT_2");
        verify(jdbcTemplate).execute("TEST_STMT_3");
    }

    @Test
    public void testExecuteSQLBuilderWithResourcePath() throws IOException {
        reset(jdbcTemplate);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                sql(new BuilderSupport<ExecuteSQLBuilder>() {
                    @Override
                    public void configure(ExecuteSQLBuilder builder) {
                        builder.jdbcTemplate(jdbcTemplate)
                                .sqlResource("classpath:com/consol/citrus/dsl/runner/script.sql");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLAction.class);
        Assert.assertEquals(test.getLastExecutedAction().getClass(), ExecuteSQLAction.class);

        ExecuteSQLAction action = (ExecuteSQLAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "sql");
        Assert.assertEquals(action.isIgnoreErrors(), false);
        Assert.assertEquals(action.getJdbcTemplate(), jdbcTemplate);
        Assert.assertEquals(action.getSqlResourcePath(), "classpath:com/consol/citrus/dsl/runner/script.sql");

        verify(jdbcTemplate).execute("TEST_STMT_1");
        verify(jdbcTemplate).execute("TEST_STMT_2");
        verify(jdbcTemplate).execute("TEST_STMT_3");
    }
}
