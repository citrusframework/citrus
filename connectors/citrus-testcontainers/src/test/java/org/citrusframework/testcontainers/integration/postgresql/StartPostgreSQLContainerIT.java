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

package org.citrusframework.testcontainers.integration.postgresql;

import java.io.IOException;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.testcontainers.integration.AbstractTestcontainersIT;
import org.citrusframework.testcontainers.postgresql.PostgreSQLSettings;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.container.FinallySequence.Builder.doFinally;
import static org.citrusframework.testcontainers.actions.TestcontainersActionBuilder.testcontainers;

public class StartPostgreSQLContainerIT extends AbstractTestcontainersIT {

    @Test
    @CitrusTest
    public void shouldStartContainer() {
        given(doFinally().actions(testcontainers().stop()
                .containerName(PostgreSQLSettings.CONTAINER_NAME_DEFAULT)));

        when(testcontainers()
                .postgreSQL()
                .start()
                .initScript("CREATE TABLE IF NOT EXISTS todo (id SERIAL PRIMARY KEY, task VARCHAR, completed INTEGER);"));

        then(this::verifyContainer);
    }

    private void verifyContainer(TestContext context) {
        try (DockerClient dockerClient = createDockerClient()) {
            InspectContainerResponse response = dockerClient.inspectContainerCmd(context.getVariable("${CITRUS_TESTCONTAINERS_POSTGRESQL_CONTAINER_ID}"))
                    .exec();

            Assert.assertNotNull(response.getState().getRunning());
            Assert.assertTrue(response.getState().getRunning());
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to verify Docker container", e);
        }
    }
}
