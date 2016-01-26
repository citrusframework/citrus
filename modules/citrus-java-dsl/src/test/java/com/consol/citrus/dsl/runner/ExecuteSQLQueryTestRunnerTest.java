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
import com.consol.citrus.actions.ExecuteSQLQueryAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.builder.BuilderSupport;
import com.consol.citrus.dsl.builder.ExecuteSQLQueryBuilder;
import com.consol.citrus.script.ScriptTypes;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.script.ScriptValidationContext;
import com.consol.citrus.validation.script.sql.SqlResultSetScriptValidator;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.*;
import java.util.*;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class ExecuteSQLQueryTestRunnerTest extends AbstractTestNGUnitTest {

    private JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
    private Resource resource = Mockito.mock(Resource.class);

    private SqlResultSetScriptValidator validator = Mockito.mock(SqlResultSetScriptValidator.class);
    
    @Test
    public void testExecuteSQLQueryWithResource() throws IOException {
        List<Map<String, Object>> results = new ArrayList<>();
        results.add(Collections.<String, Object>singletonMap("NAME", "Leonard"));

        reset(jdbcTemplate);

        when(jdbcTemplate.queryForList(anyString())).thenReturn(results)
                                                    .thenReturn(Collections.singletonList(Collections.<String, Object>singletonMap("CNT_EPISODES", "100000")));
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                variable("episodeId", "citrus:randomNumber(5)");

                query(new BuilderSupport<ExecuteSQLQueryBuilder>() {
                    @Override
                    public void configure(ExecuteSQLQueryBuilder builder) {
                        builder.jdbcTemplate(jdbcTemplate)
                                .sqlResource(new ClassPathResource("com/consol/citrus/dsl/runner/query-script.sql"))
                                .validate("NAME", "Leonard")
                                .validate("CNT_EPISODES", "100000")
                                .extract("NAME", "actorName");
                    }
                });
            }
        };

        TestContext context = builder.getTestContext();
        Assert.assertNotNull(context.getVariable("NAME"));
        Assert.assertNotNull(context.getVariable("actorName"));
        Assert.assertNotNull(context.getVariable("CNT_EPISODES"));
        Assert.assertEquals(context.getVariable("NAME"), "Leonard");
        Assert.assertEquals(context.getVariable("actorName"), "Leonard");
        Assert.assertEquals(context.getVariable("CNT_EPISODES"), "100000");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLQueryAction.class);
        
        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)test.getActions().get(0);
        
        Assert.assertEquals(action.getName(), "sql-query");
        Assert.assertEquals(action.getControlResultSet().size(), 2);
        Set<Map.Entry<String, List<String>>> rows = action.getControlResultSet().entrySet();
        Assert.assertEquals(getRow("NAME", rows).toString(), "NAME=[Leonard]");
        Assert.assertEquals(getRow("CNT_EPISODES", rows).toString(), "CNT_EPISODES=[100000]");
        Assert.assertEquals(action.getExtractVariables().size(), 1);
        Assert.assertEquals(action.getExtractVariables().entrySet().iterator().next().toString(), "NAME=actorName");
        Assert.assertNull(action.getScriptValidationContext());
        Assert.assertEquals(action.getJdbcTemplate(), jdbcTemplate);
        Assert.assertEquals(action.getStatements().size(), 2);
        Assert.assertNull(action.getSqlResourcePath());
        Assert.assertNull(action.getValidator());

    }
    
    @Test
    public void testExecuteSQLQueryWithStatements() {
        List<Map<String, Object>> results = new ArrayList<>();
        results.add(Collections.<String, Object>singletonMap("NAME", "Penny"));
        results.add(Collections.<String, Object>singletonMap("NAME", "Sheldon"));

        reset(jdbcTemplate);
        when(jdbcTemplate.queryForList("SELECT NAME FROM ACTORS")).thenReturn(results);
        when(jdbcTemplate.queryForList("SELECT COUNT(*) as CNT_EPISODES FROM EPISODES")).thenReturn(Collections.singletonList(Collections.<String, Object>singletonMap("CNT_EPISODES", "9999")));
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                query(new BuilderSupport<ExecuteSQLQueryBuilder>() {
                    @Override
                    public void configure(ExecuteSQLQueryBuilder builder) {
                        builder.jdbcTemplate(jdbcTemplate)
                                .statement("SELECT NAME FROM ACTORS")
                                .statement("SELECT COUNT(*) as CNT_EPISODES FROM EPISODES")
                                .validate("NAME", "Penny", "Sheldon")
                                .validate("CNT_EPISODES", "9999")
                                .extract("CNT_EPISODES", "cntEpisodes");
                    }
                });
            }
        };

        TestContext context = builder.getTestContext();
        Assert.assertNotNull(context.getVariable("NAME"));
        Assert.assertNotNull(context.getVariable("CNT_EPISODES"));
        Assert.assertNotNull(context.getVariable("cntEpisodes"));
        Assert.assertEquals(context.getVariable("NAME"), "Penny");
        Assert.assertEquals(context.getVariable("CNT_EPISODES"), "9999");
        Assert.assertEquals(context.getVariable("cntEpisodes"), "9999");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLQueryAction.class);
        
        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)test.getActions().get(0);
        
        Assert.assertEquals(action.getName(), "sql-query");
        Assert.assertEquals(action.getControlResultSet().size(), 2);
        Set<Map.Entry<String, List<String>>> rows = action.getControlResultSet().entrySet();
        Assert.assertEquals(getRow("NAME", rows).toString(), "NAME=[Penny, Sheldon]");
        Assert.assertEquals(getRow("CNT_EPISODES", rows).toString(), "CNT_EPISODES=[9999]");
        Assert.assertEquals(action.getExtractVariables().size(), 1);
        Assert.assertEquals(action.getExtractVariables().entrySet().iterator().next().toString(), "CNT_EPISODES=cntEpisodes");
        Assert.assertEquals(action.getStatements().size(), 2);
        Assert.assertEquals(action.getStatements().toString(), "[SELECT NAME FROM ACTORS, SELECT COUNT(*) as CNT_EPISODES FROM EPISODES]");
        Assert.assertNull(action.getScriptValidationContext());
        Assert.assertEquals(action.getJdbcTemplate(), jdbcTemplate);
        Assert.assertNull(action.getValidator());

    }

    @Test
    public void testValidationScript() {
        List<Map<String, Object>> results = new ArrayList<>();
        results.add(Collections.<String, Object>singletonMap("NAME", "Penny"));
        results.add(Collections.<String, Object>singletonMap("NAME", "Sheldon"));

        reset(jdbcTemplate);
        when(jdbcTemplate.queryForList("SELECT NAME FROM ACTORS")).thenReturn(results);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                query(new BuilderSupport<ExecuteSQLQueryBuilder>() {
                    @Override
                    public void configure(ExecuteSQLQueryBuilder builder) {
                        builder.jdbcTemplate(jdbcTemplate)
                                .statement("SELECT NAME FROM ACTORS")
                                .validateScript("assert rows[0].NAME == 'Penny'\n" +
                                        "assert rows[1].NAME == 'Sheldon'", ScriptTypes.GROOVY);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLQueryAction.class);
        
        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)test.getActions().get(0);
        
        Assert.assertEquals(action.getName(), "sql-query");
        Assert.assertEquals(action.getControlResultSet().size(), 0);
        Assert.assertEquals(action.getExtractVariables().size(), 0);
        Assert.assertNotNull(action.getScriptValidationContext());
        Assert.assertTrue(action.getScriptValidationContext().getValidationScript().startsWith("assert rows[0].NAME == 'Penny'"));
        Assert.assertNull(action.getScriptValidationContext().getValidationScriptResourcePath());
        Assert.assertEquals(action.getStatements().size(), 1);
        Assert.assertEquals(action.getStatements().toString(), "[SELECT NAME FROM ACTORS]");
        Assert.assertEquals(action.getJdbcTemplate(), jdbcTemplate);

    }
    
    @Test
    public void testValidationScriptResource() throws IOException {
        List<Map<String, Object>> results = new ArrayList<>();
        results.add(Collections.<String, Object>singletonMap("NAME", "Radj"));

        reset(jdbcTemplate, resource);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("assert rows[0].NAME == 'Radj'".getBytes()));
        when(jdbcTemplate.queryForList("SELECT NAME FROM ACTORS")).thenReturn(results);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                query(new BuilderSupport<ExecuteSQLQueryBuilder>() {
                    @Override
                    public void configure(ExecuteSQLQueryBuilder builder) {
                        builder.jdbcTemplate(jdbcTemplate)
                                .statement("SELECT NAME FROM ACTORS")
                                .validateScript(resource, ScriptTypes.GROOVY);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLQueryAction.class);
        
        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)test.getActions().get(0);
        
        Assert.assertEquals(action.getName(), "sql-query");
        Assert.assertEquals(action.getControlResultSet().size(), 0);
        Assert.assertEquals(action.getExtractVariables().size(), 0);
        Assert.assertNotNull(action.getScriptValidationContext());
        Assert.assertEquals(action.getScriptValidationContext().getValidationScript(), "assert rows[0].NAME == 'Radj'");
        Assert.assertNull(action.getScriptValidationContext().getValidationScriptResourcePath());
        Assert.assertEquals(action.getStatements().size(), 1);
        Assert.assertEquals(action.getStatements().toString(), "[SELECT NAME FROM ACTORS]");
        Assert.assertEquals(action.getJdbcTemplate(), jdbcTemplate);

    }
    
    @Test
    public void testGroovyValidationScript() {
        List<Map<String, Object>> results = new ArrayList<>();
        results.add(Collections.<String, Object>singletonMap("NAME", "Howard"));
        results.add(Collections.<String, Object>singletonMap("NAME", "Sheldon"));

        reset(jdbcTemplate);
        when(jdbcTemplate.queryForList("SELECT NAME FROM ACTORS")).thenReturn(results);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                query(new BuilderSupport<ExecuteSQLQueryBuilder>() {
                    @Override
                    public void configure(ExecuteSQLQueryBuilder builder) {
                        builder.jdbcTemplate(jdbcTemplate)
                                .statement("SELECT NAME FROM ACTORS")
                                .groovy("assert rows[0].NAME == 'Howard'");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLQueryAction.class);
        
        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)test.getActions().get(0);
        
        Assert.assertEquals(action.getName(), "sql-query");
        Assert.assertEquals(action.getControlResultSet().size(), 0);
        Assert.assertEquals(action.getExtractVariables().size(), 0);
        Assert.assertNotNull(action.getScriptValidationContext());
        Assert.assertEquals(action.getScriptValidationContext().getValidationScript(), "assert rows[0].NAME == 'Howard'");
        Assert.assertNull(action.getScriptValidationContext().getValidationScriptResourcePath());
        Assert.assertEquals(action.getStatements().size(), 1);
        Assert.assertEquals(action.getStatements().toString(), "[SELECT NAME FROM ACTORS]");
        Assert.assertEquals(action.getJdbcTemplate(), jdbcTemplate);

    }
    
    @Test
    public void testGroovyValidationScriptResource() throws IOException {
        List<Map<String, Object>> results = new ArrayList<>();
        results.add(Collections.<String, Object>singletonMap("NAME", "Penny"));
        results.add(Collections.<String, Object>singletonMap("NAME", "Howard"));
        results.add(Collections.<String, Object>singletonMap("NAME", "Sheldon"));

        reset(jdbcTemplate, resource);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("assert rows[1].NAME == 'Howard'".getBytes()));
        when(jdbcTemplate.queryForList("SELECT NAME FROM ACTORS")).thenReturn(results);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                query(new BuilderSupport<ExecuteSQLQueryBuilder>() {
                    @Override
                    public void configure(ExecuteSQLQueryBuilder builder) {
                        builder.jdbcTemplate(jdbcTemplate)
                                .statement("SELECT NAME FROM ACTORS")
                                .groovy(resource);
                    }
                });
            }
        };
        
        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLQueryAction.class);
        
        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)test.getActions().get(0);
        
        Assert.assertEquals(action.getName(), "sql-query");
        Assert.assertEquals(action.getControlResultSet().size(), 0);
        Assert.assertEquals(action.getExtractVariables().size(), 0);
        Assert.assertNotNull(action.getScriptValidationContext());
        Assert.assertEquals(action.getScriptValidationContext().getValidationScript(), "assert rows[1].NAME == 'Howard'");
        Assert.assertNull(action.getScriptValidationContext().getValidationScriptResourcePath());
        Assert.assertEquals(action.getStatements().size(), 1);
        Assert.assertEquals(action.getStatements().toString(), "[SELECT NAME FROM ACTORS]");
        Assert.assertEquals(action.getJdbcTemplate(), jdbcTemplate);

    }

    @Test
    public void testCustomScriptValidator() {
        List<Map<String, Object>> results = new ArrayList<>();
        results.add(Collections.<String, Object>singletonMap("NAME", "Howard"));
        results.add(Collections.<String, Object>singletonMap("NAME", "Penny"));
        results.add(Collections.<String, Object>singletonMap("NAME", "Sheldon"));

        reset(jdbcTemplate, validator);
        when(jdbcTemplate.queryForList("SELECT NAME FROM ACTORS")).thenReturn(results);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                query(new BuilderSupport<ExecuteSQLQueryBuilder>() {
                    @Override
                    public void configure(ExecuteSQLQueryBuilder builder) {
                        builder.jdbcTemplate(jdbcTemplate)
                                .statement("SELECT NAME FROM ACTORS")
                                .groovy("assert rows[0].NAME == 'Howard'")
                                .validator(validator);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLQueryAction.class);

        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)test.getActions().get(0);

        Assert.assertEquals(action.getName(), "sql-query");
        Assert.assertEquals(action.getControlResultSet().size(), 0);
        Assert.assertEquals(action.getExtractVariables().size(), 0);
        Assert.assertNotNull(action.getScriptValidationContext());
        Assert.assertEquals(action.getScriptValidationContext().getValidationScript(), "assert rows[0].NAME == 'Howard'");
        Assert.assertNull(action.getScriptValidationContext().getValidationScriptResourcePath());
        Assert.assertEquals(action.getStatements().size(), 1);
        Assert.assertEquals(action.getStatements().toString(), "[SELECT NAME FROM ACTORS]");
        Assert.assertEquals(action.getJdbcTemplate(), jdbcTemplate);
        Assert.assertEquals(action.getValidator(), validator);

        verify(validator).validateSqlResultSet(any(List.class), any(ScriptValidationContext.class), any(TestContext.class));
    }

    /**
     * Gets row from result set with given column name.
     * @param columnName
     * @param rows
     * @return
     */
    private Map.Entry<String, List<String>> getRow(String columnName, Set<Map.Entry<String, List<String>>> rows) {
        for (Map.Entry<String, List<String>> row : rows) {
            if (row.getKey().equals(columnName)) {
                return row;
            }
        }

        throw new AssertionError(String.format("Missing column in result set for name '%s'", columnName));
    }
}
