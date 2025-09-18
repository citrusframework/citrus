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

package org.citrusframework.cucumber.steps.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.citrusframework.Citrus;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.actions.ExecuteSQLQueryAction;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import static org.citrusframework.actions.ExecuteSQLAction.Builder.sql;
import static org.citrusframework.actions.ExecuteSQLQueryAction.Builder.query;
import static org.citrusframework.container.RepeatOnErrorUntilTrue.Builder.repeatOnError;

public class JdbcSteps {

    @CitrusResource
    private TestCaseRunner runner;

    @CitrusResource
    private TestContext context;

    @CitrusFramework
    private Citrus citrus;

    private DataSource dataSource;
    private final List<String> sqlQueryStatements = new ArrayList<>();

    private int maxRetryAttempts = JdbcSettings.getMaxAttempts();
    private long delayBetweenAttempts = JdbcSettings.getDelayBetweenAttempts();

    @Before
    public void before(Scenario scenario) {
        if (dataSource == null && citrus.getCitrusContext().getReferenceResolver().resolveAll(DataSource.class).size() == 1L) {
            dataSource = citrus.getCitrusContext().getReferenceResolver().resolve(DataSource.class);
        }
    }

    @Given("^SQL query retry configuration$")
    public void configureRetryConfiguration(Map<String, Object> configuration) {
        maxRetryAttempts = Integer.parseInt(configuration.getOrDefault("maxRetryAttempts", maxRetryAttempts).toString());
        delayBetweenAttempts = Long.parseLong(configuration.getOrDefault("delayBetweenAttempts", delayBetweenAttempts).toString());
    }

    @Given("^SQL query max retry attempts: (\\d+)")
    public void configureMaxRetryAttempts(int maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;
    }

    @Given("^SQL query retry delay: (\\d+)ms")
    public void configureDelayBetweenAttempts(long delayBetweenAttempts) {
        this.delayBetweenAttempts = delayBetweenAttempts;
    }

    @Given("^(?:D|d)ata source: ([^\"\\s]+)$")
    public void setDataSource(String id) {
        if (!citrus.getCitrusContext().getReferenceResolver().isResolvable(id)) {
            throw new CitrusRuntimeException("Unable to find data source for id: " + id);
        }

        dataSource = citrus.getCitrusContext().getReferenceResolver().resolve(id, DataSource.class);
    }

    @Given("^(?:D|d)atabase connection$")
    public void setConnection(DataTable properties) {
        Map<String, String> connectionProps = properties.asMap(String.class, String.class);

        String driver = connectionProps.getOrDefault("driver", "org.postgresql.Driver");
        String url = connectionProps.getOrDefault("url", "jdbc:postgresql://localhost:5432/testdb");
        String username = connectionProps.getOrDefault("username", "test");
        String password = connectionProps.getOrDefault("password", "test");
        boolean suppressClose = Boolean.parseBoolean(connectionProps.getOrDefault("suppressClose", Boolean.TRUE.toString()));

        SingleConnectionDataSource singleConnectionDataSource = new SingleConnectionDataSource(
                context.replaceDynamicContentInString(url),
                context.replaceDynamicContentInString(username),
                context.replaceDynamicContentInString(password), suppressClose);
        singleConnectionDataSource.setDriverClassName(context.replaceDynamicContentInString(driver));
        this.dataSource = singleConnectionDataSource;
    }

    @Given("^SQL query: (.+)$")
    public void addQueryStatement(String statement) {
        if (statement.trim().toUpperCase().startsWith("SELECT")) {
            sqlQueryStatements.add(statement);
        } else {
            throw new CitrusRuntimeException("Invalid SQL query - please use proper 'SELECT' statement");
        }
    }

    @Given("^SQL query$")
    public void addQueryStatementMultiline(String statement) {
        addQueryStatement(statement);
    }

    @Given("^SQL query statements:$")
    public void addQueryStatements(DataTable statements) {
        statements.asList().forEach(this::addQueryStatement);
    }

    @Then("^verify column ([^\"\\s]+)=(.+)$")
    public void verifyColumn(String name, String value) {
        if (maxRetryAttempts > 0) {
            runner.run(repeatOnError()
                    .until((index, context) -> index >= maxRetryAttempts)
                    .autoSleep(delayBetweenAttempts)
                    .actions(query(dataSource)
                              .statements(sqlQueryStatements)
                              .validate(name, value)));
        } else {
            runner.run(query(dataSource)
                        .statements(sqlQueryStatements)
                        .validate(name, value));
        }
        sqlQueryStatements.clear();
    }

    @Then("^verify columns$")
    public void verifyResultSet(DataTable expectedResults) {
        ExecuteSQLQueryAction.Builder action = query(dataSource)
                                                    .statements(sqlQueryStatements);

        List<List<String>> rows = expectedResults.asLists(String.class);
        rows.forEach(row -> {
            if (!row.isEmpty()) {
                String columnName = row.remove(0);
                action.validate(columnName, row.toArray(new String[]{}));
            }
        });

        if (maxRetryAttempts > 0) {
            runner.run(repeatOnError()
                    .until((index, context) -> index >= maxRetryAttempts)
                    .autoSleep(delayBetweenAttempts)
                    .actions(action));
        } else {
            runner.run(action);
        }

        sqlQueryStatements.clear();
    }

    @Then("^verify result set$")
    public void verifyResultSet(String verifyScript) {
        if (maxRetryAttempts > 0) {
            runner.run(repeatOnError()
                    .until((index, context) -> index >= maxRetryAttempts)
                    .autoSleep(delayBetweenAttempts)
                    .actions(query(dataSource)
                              .statements(sqlQueryStatements)
                              .groovy(verifyScript)));
        } else {
            runner.run(query(dataSource)
                        .statements(sqlQueryStatements)
                        .groovy(verifyScript));
        }

        sqlQueryStatements.clear();
    }

    @When("^(?:execute |perform )?SQL update: (.+)$")
    public void executeUpdate(String statement) {
        if (statement.trim().toUpperCase().startsWith("SELECT")) {
            throw new CitrusRuntimeException("Invalid SQL update statement - please use SQL query for 'SELECT' statements");
        } else {
            runner.run(sql(dataSource).statement(statement));
        }

    }

    @When("^(?:execute |perform )?SQL update$")
    public void executeUpdateMultiline(String statement) {
        executeUpdate(statement);
    }

    @When("^(?:execute |perform )?SQL updates$")
    public void executeUpdates(DataTable statements) {
        statements.asList().forEach(this::executeUpdate);
    }
}
