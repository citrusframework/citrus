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

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import io.cucumber.java.en.Given;
import org.citrusframework.testcontainers.postgresql.PostgreSQLSettings;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class SetupSteps {

    public static PostgreSQLContainer testdbContainer = new PostgreSQLContainer(
            DockerImageName.parse(PostgreSQLSettings.getImageName()).withTag(PostgreSQLSettings.getPostgreSQLVersion()))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("secret")
            .withInitScript("test-db-init.sql")
            .withCreateContainerCmdModifier(modifier -> modifier.withPortBindings(
                    new PortBinding(Ports.Binding.bindPort(PostgreSQLContainer.POSTGRESQL_PORT),
                            new ExposedPort(PostgreSQLContainer.POSTGRESQL_PORT))));

    @Given("^Start database$")
    public void startDatabase() {
        testdbContainer.start();
    }

    @Given("^Stop database$")
    public void stopDatabase() {
        testdbContainer.stop();
    }
}
