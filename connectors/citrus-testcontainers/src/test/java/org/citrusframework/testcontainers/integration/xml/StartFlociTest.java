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

package org.citrusframework.testcontainers.integration.xml;

import java.io.IOException;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.testcontainers.aws2.StartFlociAction;
import org.citrusframework.xml.XmlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;

public class StartFlociTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadTestcontainersActions() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/testcontainers/xml/start-floci.citrus.it.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("StartFlociTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(1L, result.getActionCount());
        Assertions.assertEquals(StartFlociAction.class, result.getTestAction(0).getClass());
        Assertions.assertTrue(result.getTestResult().isSuccess());

        verifyContainer(context);

        Assertions.assertTrue(context.getReferenceResolver().isResolvable("s3Client"));
        Assertions.assertNotNull(context.getReferenceResolver().resolve("s3Client", S3Client.class));
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
    }
}
