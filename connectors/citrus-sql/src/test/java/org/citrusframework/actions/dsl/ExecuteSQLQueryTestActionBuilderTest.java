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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.ExecuteSQLQueryAction;
import org.citrusframework.spi.Resources;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.actions.ExecuteSQLQueryAction.Builder.query;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class ExecuteSQLQueryTestActionBuilderTest extends UnitTestSupport {

    private final JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
    private final PlatformTransactionManager transactionManager = Mockito.mock(PlatformTransactionManager.class);

    @Test
    public void testExecuteSQLQueryWithResource() {
        List<Map<String, Object>> results = new ArrayList<>();
        results.add(Collections.singletonMap("NAME", "Leonard"));

        reset(jdbcTemplate);

        when(jdbcTemplate.queryForList(anyString())).thenReturn(results)
                .thenReturn(Collections.singletonList(Collections.singletonMap("CNT_EPISODES", "100000")));
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.variable("episodeId", "citrus:randomNumber(5)");

        builder.$(query().jdbcTemplate(jdbcTemplate)
                .sqlResource(Resources.fromClasspath("org/citrusframework/actions/dsl/query-script.sql"))
                .validate("NAME", "Leonard")
                .validate("CNT_EPISODES", "100000")
                .extract("NAME", "actorName")
                .extract("CNT_EPISODES", "episodesCount"));

        Assert.assertNotNull(context.getVariable("actorName"));
        Assert.assertNotNull(context.getVariable("episodesCount"));
        Assert.assertEquals(context.getVariable("actorName"), "Leonard");
        Assert.assertEquals(context.getVariable("episodesCount"), "100000");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLQueryAction.class);

        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction) test.getActions().get(0);

        Assert.assertEquals(action.getName(), "sql-query");
        Assert.assertEquals(action.getControlResultSet().size(), 2);
        Set<Map.Entry<String, List<String>>> rows = action.getControlResultSet().entrySet();
        Assert.assertEquals(getRow("NAME", rows).toString(), "NAME=[Leonard]");
        Assert.assertEquals(getRow("CNT_EPISODES", rows).toString(), "CNT_EPISODES=[100000]");
        Assert.assertEquals(action.getExtractVariables().size(), 2);
        Assert.assertEquals(action.getExtractVariables().get("NAME"), "actorName");
        Assert.assertEquals(action.getExtractVariables().get("CNT_EPISODES"), "episodesCount");
        Assert.assertNull(action.getScriptValidationContext());
        Assert.assertEquals(action.getJdbcTemplate(), jdbcTemplate);
        Assert.assertEquals(action.getStatements().size(), 2);
        Assert.assertNull(action.getSqlResourcePath());
        Assert.assertNull(action.getValidator());
    }

    @Test
    public void testExecuteSQLQueryWithStatements() {
        List<Map<String, Object>> results = new ArrayList<>();
        results.add(Collections.singletonMap("NAME", "Penny"));
        results.add(Collections.singletonMap("NAME", "Sheldon"));

        reset(jdbcTemplate);
        when(jdbcTemplate.queryForList("SELECT NAME FROM ACTORS")).thenReturn(results);
        when(jdbcTemplate.queryForList("SELECT COUNT(*) as CNT_EPISODES FROM EPISODES")).thenReturn(Collections.singletonList(Collections.singletonMap("CNT_EPISODES", "9999")));
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(query().jdbcTemplate(jdbcTemplate)
                .statement("SELECT NAME FROM ACTORS")
                .statement("SELECT COUNT(*) as CNT_EPISODES FROM EPISODES")
                .validate("NAME", "Penny", "Sheldon")
                .validate("CNT_EPISODES", "9999")
                .extract("NAME", "actorName")
                .extract("CNT_EPISODES", "episodesCount"));

        Assert.assertNotNull(context.getVariable("actorName"));
        Assert.assertNotNull(context.getVariable("episodesCount"));
        Assert.assertEquals(context.getVariable("actorName"), "Penny;Sheldon");
        Assert.assertEquals(context.getVariable("episodesCount"), "9999");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLQueryAction.class);

        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction) test.getActions().get(0);

        Assert.assertEquals(action.getName(), "sql-query");
        Assert.assertEquals(action.getControlResultSet().size(), 2);
        Set<Map.Entry<String, List<String>>> rows = action.getControlResultSet().entrySet();
        Assert.assertEquals(getRow("NAME", rows).toString(), "NAME=[Penny, Sheldon]");
        Assert.assertEquals(getRow("CNT_EPISODES", rows).toString(), "CNT_EPISODES=[9999]");
        Assert.assertEquals(action.getExtractVariables().size(), 2);
        Assert.assertEquals(action.getExtractVariables().get("NAME"), "actorName");
        Assert.assertEquals(action.getExtractVariables().get("CNT_EPISODES"), "episodesCount");
        Assert.assertEquals(action.getStatements().size(), 2);
        Assert.assertEquals(action.getStatements().toString(), "[SELECT NAME FROM ACTORS, SELECT COUNT(*) as CNT_EPISODES FROM EPISODES]");
        Assert.assertNull(action.getScriptValidationContext());
        Assert.assertEquals(action.getJdbcTemplate(), jdbcTemplate);
        Assert.assertNull(action.getValidator());

    }

    @Test
    public void testExecuteSQLQueryWithTransaction() {
        List<Map<String, Object>> results = new ArrayList<>();
        results.add(Collections.singletonMap("NAME", "Penny"));
        results.add(Collections.singletonMap("NAME", "Sheldon"));

        reset(jdbcTemplate, transactionManager);
        when(jdbcTemplate.queryForList("SELECT NAME FROM ACTORS")).thenReturn(results);
        when(jdbcTemplate.queryForList("SELECT COUNT(*) as CNT_EPISODES FROM EPISODES")).thenReturn(Collections.singletonList(Collections.singletonMap("CNT_EPISODES", "9999")));
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(query().jdbcTemplate(jdbcTemplate)
                .transactionManager(transactionManager)
                .transactionTimeout(5000)
                .transactionIsolationLevel("ISOLATION_READ_COMMITTED")
                .statement("SELECT NAME FROM ACTORS")
                .statement("SELECT COUNT(*) as CNT_EPISODES FROM EPISODES")
                .validate("NAME", "Penny", "Sheldon")
                .validate("CNT_EPISODES", "9999")
                .extract("NAME", "actorName")
                .extract("CNT_EPISODES", "episodesCount"));

        Assert.assertNotNull(context.getVariable("actorName"));
        Assert.assertNotNull(context.getVariable("episodesCount"));
        Assert.assertEquals(context.getVariable("actorName"), "Penny;Sheldon");
        Assert.assertEquals(context.getVariable("episodesCount"), "9999");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ExecuteSQLQueryAction.class);

        ExecuteSQLQueryAction action = (ExecuteSQLQueryAction) test.getActions().get(0);

        Assert.assertEquals(action.getName(), "sql-query");
        Assert.assertEquals(action.getControlResultSet().size(), 2);
        Set<Map.Entry<String, List<String>>> rows = action.getControlResultSet().entrySet();
        Assert.assertEquals(getRow("NAME", rows).toString(), "NAME=[Penny, Sheldon]");
        Assert.assertEquals(getRow("CNT_EPISODES", rows).toString(), "CNT_EPISODES=[9999]");
        Assert.assertEquals(action.getExtractVariables().size(), 2);
        Assert.assertEquals(action.getExtractVariables().get("NAME"), "actorName");
        Assert.assertEquals(action.getExtractVariables().get("CNT_EPISODES"), "episodesCount");
        Assert.assertEquals(action.getStatements().size(), 2);
        Assert.assertEquals(action.getStatements().toString(), "[SELECT NAME FROM ACTORS, SELECT COUNT(*) as CNT_EPISODES FROM EPISODES]");
        Assert.assertNull(action.getScriptValidationContext());
        Assert.assertEquals(action.getJdbcTemplate(), jdbcTemplate);
        Assert.assertEquals(action.getTransactionManager(), transactionManager);
        Assert.assertEquals(action.getTransactionTimeout(), "5000");
        Assert.assertEquals(action.getTransactionIsolationLevel(), "ISOLATION_READ_COMMITTED");
        Assert.assertNull(action.getValidator());
    }

    /**
     * Gets row from result set with given column name.
     *
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
