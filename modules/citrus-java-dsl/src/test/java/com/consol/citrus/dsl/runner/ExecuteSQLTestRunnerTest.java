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
import org.easymock.EasyMock;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class ExecuteSQLTestRunnerTest extends AbstractTestNGUnitTest {
    private JdbcTemplate jdbcTemplate = EasyMock.createMock(JdbcTemplate.class);
    private Resource resource = EasyMock.createMock(Resource.class);
    private File file = EasyMock.createMock(File.class);
    
    @Test
    public void TestExecuteSQLBuilderWithStatement() {
        reset(jdbcTemplate);
        jdbcTemplate.execute("TEST_STMT_1");
        expectLastCall().once();
        jdbcTemplate.execute("TEST_STMT_2");
        expectLastCall().once();
        jdbcTemplate.execute("TEST_STMT_3");
        expectLastCall().once();
        replay(jdbcTemplate);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
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

        verify(jdbcTemplate);
    }
    
    @Test
    public void TestExecuteSQLBuilderWithResource() throws IOException {
        reset(jdbcTemplate, resource, file);
        expect(resource.getFile()).andReturn(file).once();
        expect(file.getAbsolutePath()).andReturn("classpath:com/consol/citrus/dsl/runner/script.sql").once();

        jdbcTemplate.execute("TEST_STMT_1");
        expectLastCall().once();
        jdbcTemplate.execute("TEST_STMT_2");
        expectLastCall().once();
        jdbcTemplate.execute("TEST_STMT_3");
        expectLastCall().once();
        replay(jdbcTemplate, resource, file);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                sql(new BuilderSupport<ExecuteSQLBuilder>() {
                    @Override
                    public void configure(ExecuteSQLBuilder builder) {
                        builder.jdbcTemplate(jdbcTemplate)
                                .sqlResource(resource)
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
        Assert.assertEquals(action.getSqlResourcePath(), "classpath:com/consol/citrus/dsl/runner/script.sql");
        
        verify(jdbcTemplate, resource, file);
    }

    @Test
    public void TestExecuteSQLBuilderWithResourcePath() throws IOException {
        reset(jdbcTemplate);
        jdbcTemplate.execute("TEST_STMT_1");
        expectLastCall().once();
        jdbcTemplate.execute("TEST_STMT_2");
        expectLastCall().once();
        jdbcTemplate.execute("TEST_STMT_3");
        expectLastCall().once();
        replay(jdbcTemplate);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
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

        verify(jdbcTemplate);
    }
}
