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
import org.citrusframework.testcontainers.redpanda.RedpandaSettings;
import org.testcontainers.redpanda.RedpandaContainer;

import static org.citrusframework.testcontainers.actions.TestcontainersActionBuilder.testcontainers;

public class RedpandaSteps {

    @CitrusFramework
    private Citrus citrus;

    @CitrusResource
    private TestCaseRunner runner;

    @CitrusResource
    private TestContext context;

    public static final int REDPANDA_PORT = 9092;

    private String redpandaVersion = RedpandaSettings.getRedpandaVersion();

    private int startupTimeout = RedpandaSettings.getStartupTimeout();

    private Map<String, String> env = new HashMap<>();

    private String serviceName = RedpandaSettings.getServiceName();

    @Before
    public void before(Scenario scenario) {
        if (citrus.getCitrusContext().getReferenceResolver().isResolvable(RedpandaSettings.getContainerName(), RedpandaContainer.class)) {
            Object redpandaContainer = citrus.getCitrusContext().getReferenceResolver().resolve(RedpandaSettings.getContainerName());
            RedpandaSettings.exposeConnectionSettings((RedpandaContainer) redpandaContainer, serviceName, context);
        }
    }

    @Given("^Redpanda version (^\\s+)$")
    public void setRedpandaVersion(String version) {
        this.redpandaVersion = version;
    }

    @Given("^Redpanda service name (^\\s+)$")
    public void setRedpandaServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Given("^Redpanda startup timeout is (\\d+)(?: s| seconds)$")
    public void setStartupTimeout(int timeout) {
        this.startupTimeout = timeout;
    }

    @Given("^Redpanda env settings$")
    public void setEnvSettings(DataTable settings) {
        this.env.putAll(settings.asMap());
    }

    @Given("^start Redpanda container$")
    public void startRedpanda() {
        runner.run(testcontainers()
                .redpanda()
                .start()
                .version(redpandaVersion)
                .serviceName(serviceName)
                .withStartupTimeout(startupTimeout)
                .withEnv(env)
                .autoRemove(TestContainersSteps.autoRemoveResources));
    }

    @Given("^stop Redpanda container$")
    public void stopRedpanda() {
        runner.run(testcontainers()
                .stop()
                .containerName(RedpandaSettings.getContainerName()));

        env = new HashMap<>();
    }
}
