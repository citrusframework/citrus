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
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.testcontainers.aws2.LocalStackContainer;
import org.citrusframework.testcontainers.aws2.LocalStackSettings;
import org.citrusframework.testcontainers.integration.AbstractTestcontainersIT;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.container.FinallySequence.Builder.doFinally;
import static org.citrusframework.testcontainers.actions.TestcontainersActionBuilder.testcontainers;

public class StartLocalStackContainerIT extends AbstractTestcontainersIT {

    @Test
    @CitrusTest
    public void shouldStartContainer() {
        given(doFinally().actions(testcontainers().stop()
                .containerName(LocalStackSettings.CONTAINER_NAME_DEFAULT)));

        when(testcontainers()
                .localstack()
                .start()
                .withService(LocalStackContainer.Service.S3));

        then(this::verifyContainer);
    }

    private void verifyContainer(TestContext context) {
        try (DockerClient dockerClient = createDockerClient()) {
            InspectContainerResponse response = dockerClient.inspectContainerCmd(context.getVariable("${CITRUS_TESTCONTAINERS_LOCALSTACK_CONTAINER_ID}"))
                    .exec();

            Assert.assertNotNull(response.getState().getRunning());
            Assert.assertTrue(response.getState().getRunning());
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to verify Docker container", e);
        }

        // verify auto created S3 client
        Assert.assertTrue(context.getReferenceResolver().isResolvable("s3Client"));
    }
}
