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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.validation.script.sql.SqlResultSetValidator;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Contract wiring for the plain-Java result-set validator (T2): the builder accepts it
 * and the built action exposes it. Validation behavior itself is T3.
 */
public class SqlResultSetValidatorTest {

    private static final String DB_STMT = "select STATUS from orders where ID = 5";

    private JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);

    private ExecuteSQLQueryAction.Builder builder;

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
}
