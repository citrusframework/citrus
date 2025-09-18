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
import org.citrusframework.testcontainers.mongodb.MongoDBSettings;
import org.testcontainers.containers.MongoDBContainer;

import static org.citrusframework.testcontainers.actions.TestcontainersActionBuilder.testcontainers;

public class MongoDBSteps {

    @CitrusFramework
    private Citrus citrus;

    @CitrusResource
    private TestCaseRunner runner;

    @CitrusResource
    private TestContext context;

    private String mongoDBVersion = MongoDBSettings.getMongoDBVersion();
    private int startupTimeout = MongoDBSettings.getStartupTimeout();

    private Map<String, String> env = new HashMap<>();

    private String serviceName = MongoDBSettings.getServiceName();

    @Before
    public void before(Scenario scenario) {
        if (citrus.getCitrusContext().getReferenceResolver().isResolvable(MongoDBSettings.getContainerName(), MongoDBContainer.class)) {
            Object mongoDBContainer = citrus.getCitrusContext().getReferenceResolver().resolve(MongoDBSettings.getContainerName());
            MongoDBSettings.exposeConnectionSettings((MongoDBContainer) mongoDBContainer, serviceName, context);
        }
    }

    @Given("^MongoDB version (^\\s+)$")
    public void setMongoDBVersion(String version) {
        this.mongoDBVersion = version;
    }

    @Given("^MongoDB service name (^\\s+)$")
    public void setMongoDBServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Given("^MongoDB startup timeout is (\\d+)(?: s| seconds)$")
    public void setStartupTimeout(int timeout) {
        this.startupTimeout = timeout;
    }

    @Given("^MongoDB env settings$")
    public void setEnvSettings(DataTable settings) {
        this.env.putAll(settings.asMap());
    }

    @Given("^start MongoDB container$")
    public void startMongo() {
        runner.run(testcontainers()
                .mongoDB()
                .start()
                .version(mongoDBVersion)
                .serviceName(serviceName)
                .withStartupTimeout(startupTimeout)
                .withEnv(env)
                .autoRemove(TestContainersSteps.autoRemoveResources));
    }

    @Given("^stop MongoDB container$")
    public void stopMongo() {
        runner.run(testcontainers()
                .stop()
                .containerName(MongoDBSettings.getContainerName()));

        env = new HashMap<>();
    }
}
