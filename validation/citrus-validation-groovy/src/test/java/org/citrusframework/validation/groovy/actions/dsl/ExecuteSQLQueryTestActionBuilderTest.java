/*
 * Copyright the original author or authors.
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

package org.citrusframework.validation.groovy.actions.dsl;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.TestCase;
import org.citrusframework.validation.groovy.UnitTestSupport;
import org.citrusframework.sql.actions.ExecuteSQLQueryAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.script.ScriptTypes;
import org.citrusframework.spi.Resource;
import org.citrusframework.validation.context.script.DefaultScriptValidationContext;
import org.citrusframework.validation.context.script.ScriptValidationContext;
import org.citrusframework.validation.script.sql.SqlResultSetScriptValidator;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since 2.3
 */
public class ExecuteSQLQueryTestActionBuilderTest extends UnitTestSupport implements TestActionSupport {

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
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ExecuteSQLQueryAction.class, test.getActions().get(0).getClass());

        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)test.getActions().get(0);

        Assertions.assertEquals("sql-query", action.getName());
        Assertions.assertEquals(0, action.getControlResultSet().size());
        Assertions.assertEquals(0, action.getExtractVariables().size());
        Assertions.assertNotNull(action.getScriptValidationContext());
        DefaultScriptValidationContext scriptValidationContext = (DefaultScriptValidationContext)action.getScriptValidationContext();
        Assertions.assertTrue(scriptValidationContext.getValidationScript().startsWith("assert rows[0].NAME == 'Penny'"));
        Assertions.assertNull(scriptValidationContext.getValidationScriptResourcePath());
        Assertions.assertEquals(1, action.getStatements().size());
        Assertions.assertEquals("[SELECT NAME FROM ACTORS]", action.getStatements().toString());
        Assertions.assertEquals(jdbcTemplate, action.getJdbcTemplate());

    }

    @Test
    public void testValidationScriptResource() {
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
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ExecuteSQLQueryAction.class, test.getActions().get(0).getClass());

        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)test.getActions().get(0);

        Assertions.assertEquals("sql-query", action.getName());
        Assertions.assertEquals(0, action.getControlResultSet().size());
        Assertions.assertEquals(0, action.getExtractVariables().size());
        Assertions.assertNotNull(action.getScriptValidationContext());
        DefaultScriptValidationContext scriptValidationContext = (DefaultScriptValidationContext)action.getScriptValidationContext();
        Assertions.assertEquals("assert rows[0].NAME == 'Radj'", scriptValidationContext.getValidationScript());
        Assertions.assertNull(scriptValidationContext.getValidationScriptResourcePath());
        Assertions.assertEquals(1, action.getStatements().size());
        Assertions.assertEquals("[SELECT NAME FROM ACTORS]", action.getStatements().toString());
        Assertions.assertEquals(jdbcTemplate, action.getJdbcTemplate());

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
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ExecuteSQLQueryAction.class, test.getActions().get(0).getClass());

        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)test.getActions().get(0);

        Assertions.assertEquals("sql-query", action.getName());
        Assertions.assertEquals(0, action.getControlResultSet().size());
        Assertions.assertEquals(0, action.getExtractVariables().size());
        Assertions.assertNotNull(action.getScriptValidationContext());
        DefaultScriptValidationContext scriptValidationContext = (DefaultScriptValidationContext)action.getScriptValidationContext();
        Assertions.assertEquals("assert rows[0].NAME == 'Howard'", scriptValidationContext.getValidationScript());
        Assertions.assertNull(scriptValidationContext.getValidationScriptResourcePath());
        Assertions.assertEquals(1, action.getStatements().size());
        Assertions.assertEquals("[SELECT NAME FROM ACTORS]", action.getStatements().toString());
        Assertions.assertEquals(jdbcTemplate, action.getJdbcTemplate());
    }

    @Test
    public void testGroovyValidationScriptResource() {
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
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ExecuteSQLQueryAction.class, test.getActions().get(0).getClass());

        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)test.getActions().get(0);

        Assertions.assertEquals("sql-query", action.getName());
        Assertions.assertEquals(0, action.getControlResultSet().size());
        Assertions.assertEquals(0, action.getExtractVariables().size());
        Assertions.assertNotNull(action.getScriptValidationContext());
        DefaultScriptValidationContext scriptValidationContext = (DefaultScriptValidationContext)action.getScriptValidationContext();
        Assertions.assertEquals("assert rows[1].NAME == 'Howard'", scriptValidationContext.getValidationScript());
        Assertions.assertNull(scriptValidationContext.getValidationScriptResourcePath());
        Assertions.assertEquals(1, action.getStatements().size());
        Assertions.assertEquals("[SELECT NAME FROM ACTORS]", action.getStatements().toString());
        Assertions.assertEquals(jdbcTemplate, action.getJdbcTemplate());

    }

    @Test
    @SuppressWarnings("unchecked")
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
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ExecuteSQLQueryAction.class, test.getActions().get(0).getClass());

        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)test.getActions().get(0);

        Assertions.assertEquals("sql-query", action.getName());
        Assertions.assertEquals(0, action.getControlResultSet().size());
        Assertions.assertEquals(0, action.getExtractVariables().size());
        Assertions.assertNotNull(action.getScriptValidationContext());
        DefaultScriptValidationContext scriptValidationContext = (DefaultScriptValidationContext)action.getScriptValidationContext();
        Assertions.assertEquals("assert rows[0].NAME == 'Howard'", scriptValidationContext.getValidationScript());
        Assertions.assertNull(scriptValidationContext.getValidationScriptResourcePath());
        Assertions.assertEquals(1, action.getStatements().size());
        Assertions.assertEquals("[SELECT NAME FROM ACTORS]", action.getStatements().toString());
        Assertions.assertEquals(jdbcTemplate, action.getJdbcTemplate());
        Assertions.assertEquals(validator, action.getValidator());

        verify(validator).validateSqlResultSet(any(List.class), any(ScriptValidationContext.class), any(TestContext.class));
    }
}
