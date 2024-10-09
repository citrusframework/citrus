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
import org.citrusframework.testcontainers.redpanda.StartRedpandaAction;
import org.citrusframework.xml.XmlTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class StartRedpandaTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadTestcontainersActions() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/testcontainers/xml/start-redpanda-test.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "StartRedpandaTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 1L);
        Assert.assertEquals(result.getTestAction(0).getClass(), StartRedpandaAction.class);
        Assert.assertTrue(result.getTestResult().isSuccess());

        verifyContainer(context);
    }

    private void verifyContainer(TestContext context) {
        try (DockerClient dockerClient = createDockerClient()) {
            InspectContainerResponse response = dockerClient.inspectContainerCmd(context.getVariable("${CITRUS_TESTCONTAINERS_REDPANDA_CONTAINER_ID}"))
                    .exec();

            Assert.assertNotNull(response.getState().getRunning());
            Assert.assertTrue(response.getState().getRunning());
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to verify Docker container", e);
        }
    }
}
