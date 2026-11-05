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

package org.citrusframework.testcontainers.integration.aws2;

import java.io.IOException;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.api.actions.testcontainers.aws2.AwsService;
import org.citrusframework.testcontainers.aws2.FlociSettings;
import org.citrusframework.testcontainers.integration.AbstractTestcontainersIT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StartFlociContainerIT extends AbstractTestcontainersIT implements TestActionSupport {

    @Test
    @CitrusTest
    public void shouldStartContainer(@CitrusResource TestCaseRunner runner) {
        runner.given(doFinally().actions(testcontainers().stop()
                .containerName(FlociSettings.CONTAINER_NAME_DEFAULT)));

        runner.when(testcontainers()
                .floci()
                .start()
                .withService(AwsService.S3));

        runner.then(this::verifyContainer);
    }

    private void verifyContainer(TestContext context) {
        try (DockerClient dockerClient = createDockerClient()) {
            InspectContainerResponse response = dockerClient.inspectContainerCmd(context.getVariable("${CITRUS_TESTCONTAINERS_FLOCI_CONTAINER_ID}"))
                    .exec();

            Assertions.assertNotNull(response.getState().getRunning());
            Assertions.assertTrue(response.getState().getRunning());
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to verify Docker container", e);
        }

        // verify auto created S3 client
        Assertions.assertTrue(context.getReferenceResolver().isResolvable("s3Client"));
    }
}
