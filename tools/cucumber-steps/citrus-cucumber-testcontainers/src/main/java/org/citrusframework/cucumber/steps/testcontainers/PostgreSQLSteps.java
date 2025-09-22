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

package org.citrusframework.cucumber.steps.testcontainers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import org.citrusframework.Citrus;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.cucumber.steps.util.ResourceUtils;
import org.citrusframework.testcontainers.postgresql.PostgreSQLSettings;
import org.citrusframework.util.FileUtils;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.citrusframework.testcontainers.actions.TestcontainersActionBuilder.testcontainers;

public class PostgreSQLSteps {

    @CitrusFramework
    private Citrus citrus;

    @CitrusResource
    private TestCaseRunner runner;

    @CitrusResource
    private TestContext context;

    private String postgreSQLVersion = PostgreSQLSettings.getPostgreSQLVersion();

    private String databaseName = PostgreSQLSettings.getDatabaseName();
    private String username = PostgreSQLSettings.getUsername();
    private String password = PostgreSQLSettings.getPassword();

    private int startupTimeout = PostgreSQLSettings.getStartupTimeout();

    private Map<String, String> env = new HashMap<>();

    private String serviceName = PostgreSQLSettings.getServiceName();

    private String initScript;

    @Before
    public void before(Scenario scenario) {
        if (citrus.getCitrusContext().getReferenceResolver().isResolvable(PostgreSQLSettings.getContainerName(), PostgreSQLContainer.class)) {
            PostgreSQLContainer<?> postgreSQLContainer = citrus.getCitrusContext().getReferenceResolver().resolve(PostgreSQLSettings.getContainerName(), PostgreSQLContainer.class);
            PostgreSQLSettings.exposeConnectionSettings(postgreSQLContainer, serviceName, context);
        }
    }

    @Given("^PostgreSQL version (^\\s+)$")
    public void setPostgreSQLVersion(String version) {
        this.postgreSQLVersion = version;
    }

    @Given("^PostgreSQL service name (^\\s+)$")
    public void setPostgreSQLServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Given("^PostgreSQL startup timeout is (\\d+)(?: s| seconds)$")
    public void setStartupTimeout(int timeout) {
        this.startupTimeout = timeout;
    }

    @Given("^PostgreSQL database name (^\\s+)$")
    public void setDatabaseName(String name) {
        this.databaseName = name;
    }

    @Given("^PostgreSQL username (^\\s+)$")
    public void setUsername(String name) {
        this.username = name;
    }

    @Given("^PostgreSQL password (^\\s+)$")
    public void setPassword(String password) {
        this.password = password;
    }

    @Given("^PostgreSQL env settings$")
    public void setEnvSettings(DataTable settings) {
        this.env.putAll(settings.asMap());
    }

    @Given("^start PostgreSQL container$")
    public void startPostgresql() {
        runner.run(testcontainers()
                .postgreSQL()
                .start()
                .version(postgreSQLVersion)
                .serviceName(serviceName)
                .databaseName(databaseName)
                .username(username)
                .password(password)
                .withStartupTimeout(startupTimeout)
                .withEnv(env)
                .initScript(initScript)
                .autoRemove(TestContainersSteps.autoRemoveResources));
    }

    @Given("^stop PostgreSQL container$")
    public void stopPostgresql() {
        runner.run(testcontainers()
                .stop()
                .containerName(PostgreSQLSettings.getContainerName()));

        env = new HashMap<>();
        initScript = null;
    }

    @Given("^(?:D|d)atabase init script$")
    public void setInitScript(String initScript) {
        this.initScript = initScript;
    }

    @Given("^load database init script ([^\\s]+)$")
    public void loadInitScript(String file) throws IOException {
        this.initScript = FileUtils.readToString(ResourceUtils.resolve(file, context));
    }
}
