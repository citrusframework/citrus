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

package org.citrusframework.actions.dsl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.ExecuteSQLQueryAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.script.ScriptTypes;
import org.citrusframework.spi.Resource;
import org.citrusframework.validation.script.ScriptValidationContext;
import org.citrusframework.validation.script.sql.SqlResultSetScriptValidator;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.actions.ExecuteSQLQueryAction.Builder.query;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class ExecuteSQLQueryTestActionBuilderTest extends UnitTestSupport {

    private final JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
    private final Resource resource = Mockito.mock(Resource.class);

    private final SqlResultSetScriptValidator validator = Mockito.mock(SqlResultSetScriptValidator.class);

    @Test
    public void testValidationScript() {
        List<Map<String, Object>> results = new ArrayList<>();
        results.add(Collections.<String, Object>singletonMap("NAME", "Penny"));
        results.add(Collections.<String, Object>singletonMap("NAME", "Sheldon"));

        reset(jdbcTemplate);
        when(jdbcTemplate.queryForList("SELECT NAME FROM ACTORS")).thenReturn(results);
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(query().jdbcTemplate(jdbcTemplate)
            .statement("SELECT NAME FROM ACTORS")
            .validateScript("assert rows[0].NAME == 'Penny'\n" +
                    "assert rows[1].NAME == 'Sheldon'", ScriptTypes.GROOVY));

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
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("assert rows[0].NAME == 'Radj'".getBytes()));
        when(jdbcTemplate.queryForList("SELECT NAME FROM ACTORS")).thenReturn(results);
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(query().jdbcTemplate(jdbcTemplate)
            .statement("SELECT NAME FROM ACTORS")
            .validateScript(resource, ScriptTypes.GROOVY));

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
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(query().jdbcTemplate(jdbcTemplate)
            .statement("SELECT NAME FROM ACTORS")
            .groovy("assert rows[0].NAME == 'Howard'"));

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
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("assert rows[1].NAME == 'Howard'".getBytes()));
        when(jdbcTemplate.queryForList("SELECT NAME FROM ACTORS")).thenReturn(results);
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(query().jdbcTemplate(jdbcTemplate)
            .statement("SELECT NAME FROM ACTORS")
            .groovy(resource));

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
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(query().jdbcTemplate(jdbcTemplate)
            .statement("SELECT NAME FROM ACTORS")
            .groovy("assert rows[0].NAME == 'Howard'")
            .validator(validator));

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
}
