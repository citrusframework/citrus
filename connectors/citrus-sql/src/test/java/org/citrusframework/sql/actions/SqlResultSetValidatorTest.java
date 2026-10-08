/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.sql.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.script.ScriptTypes;
import org.citrusframework.sql.UnitTestSupport;
import org.citrusframework.validation.script.sql.SqlResultSetScriptValidator;
import org.citrusframework.validation.script.sql.SqlResultSetValidator;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Plain-Java result-set validator (T2 wiring, T3 behavior): the builder accepts it and
 * the built action exposes it; the action invokes it with or without a script context.
 */
public class SqlResultSetValidatorTest extends UnitTestSupport {

    private static final String DB_STMT = "select STATUS from orders where ID = 5";

    private JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
    private SqlResultSetScriptValidator resultSetScriptValidator = Mockito.mock(SqlResultSetScriptValidator.class);

    private ExecuteSQLQueryAction.Builder builder;

    @Override
    protected TestContextFactory createTestContextFactory() {
        TestContextFactory factory = super.createTestContextFactory();
        factory.getReferenceResolver().bind("sqlResultSetScriptValidator", resultSetScriptValidator);
        return factory;
    }

    @BeforeMethod
    public void setUp() {
        builder = new ExecuteSQLQueryAction.Builder()
                .jdbcTemplate(jdbcTemplate);
    }

    @Test
    public void builderAcceptsPlainValidator() {
        SqlResultSetValidator validator = new SqlResultSetValidator() {
            @Override
            public void validateSqlResultSet(List<Map<String, Object>> resultSet, TestContext context) {
            }
        };

        ExecuteSQLQueryAction action = builder
                .statements(Collections.singletonList(DB_STMT))
                .validator(validator)
                .build();

        Assert.assertSame(action.getResultSetValidator(), validator);
    }

    @Test
    public void plainValidatorLambdaBindsWithoutScript() {
        SqlResultSetValidator validator = (resultSet, context) -> {
        };

        ExecuteSQLQueryAction action = builder
                .statements(Collections.singletonList(DB_STMT))
                .validator(validator)
                .build();

        Assert.assertSame(action.getResultSetValidator(), validator);
        Assert.assertNull(action.getScriptValidationContext());
    }

    @Test
    public void noValidatorByDefault() {
        ExecuteSQLQueryAction action = builder
                .statements(Collections.singletonList(DB_STMT))
                .build();

        Assert.assertNull(action.getResultSetValidator());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void plainValidatorRunsWithoutScriptContext() {
        reset(jdbcTemplate);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("STATUS", "in_progress");

        when(jdbcTemplate.queryForList(DB_STMT)).thenReturn(Collections.singletonList(resultMap));

        boolean[] called = {false};
        List<Map<String, Object>> captured = new ArrayList<>();

        builder.statements(Collections.singletonList(DB_STMT))
                .validator((resultSet, context) -> {
                    called[0] = true;
                    captured.addAll(resultSet);
                })
                .build().execute(context);

        Assert.assertTrue(called[0], "plain validator never ran without a script context (#619)");
        Assert.assertEquals(captured.size(), 1);
        Assert.assertEquals(captured.get(0).get("STATUS"), "in_progress");
    }

    @Test
    public void plainValidatorReceivesEmptyResultSet() {
        reset(jdbcTemplate);

        when(jdbcTemplate.queryForList(DB_STMT)).thenReturn(Collections.emptyList());

        boolean[] called = {false};
        List<Map<String, Object>> captured = new ArrayList<>();

        builder.statements(Collections.singletonList(DB_STMT))
                .validator((resultSet, context) -> {
                    called[0] = true;
                    captured.addAll(resultSet);
                })
                .build().execute(context);

        Assert.assertTrue(called[0], "plain validator never ran on an empty result set");
        Assert.assertTrue(captured.isEmpty());
    }

    @Test
    public void plainValidatorFailureFailsAction() {
        reset(jdbcTemplate);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("STATUS", "in_progress");

        when(jdbcTemplate.queryForList(DB_STMT)).thenReturn(Collections.singletonList(resultMap));

        try {
            builder.statements(Collections.singletonList(DB_STMT))
                    .validator((resultSet, context) -> {
                        throw new ValidationException("plain validation failed");
                    })
                    .build().execute(context);
        } catch (ValidationException e) {
            Assert.assertEquals(e.getMessage(), "plain validation failed");
            return;
        }

        Assert.fail("Expected ValidationException from plain validator");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void plainValidatorAndScriptValidationCompose() {
        reset(jdbcTemplate, resultSetScriptValidator);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("STATUS", "in_progress");

        when(jdbcTemplate.queryForList(DB_STMT)).thenReturn(Collections.singletonList(resultMap));

        boolean[] called = {false};

        builder.statements(Collections.singletonList(DB_STMT))
                .validateScript("assert true", ScriptTypes.GROOVY)
                .validator((resultSet, context) -> called[0] = true)
                .build().execute(context);

        Assert.assertTrue(called[0], "plain validator did not run alongside script validation");
        verify(resultSetScriptValidator).validateSqlResultSet(any(List.class),
                any(org.citrusframework.validation.context.script.ScriptValidationContext.class), eq(context));
    }
}
