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

package org.citrusframework.testcontainers.integration.yaml;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.testcontainers.actions.StopTestcontainersAction;
import org.citrusframework.yaml.YamlTestLoader;
import org.testcontainers.containers.GenericContainer;
import org.testng.Assert;
import org.testng.annotations.Test;

public class StopContainerTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadTestcontainersActions() {
        try (GenericContainer<?> busyBox = new GenericContainer("busybox:latest")
                .withCommand("/bin/sh", "-ec", "while :; do echo 'Hello World'; sleep 5 ; done")) {

            busyBox.start();
            Assert.assertTrue(busyBox.isRunning());

            context.getReferenceResolver().bind("my-container", busyBox);

            YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/testcontainers/yaml/stop-container-test.yaml");

            testLoader.load();
            TestCase result = testLoader.getTestCase();
            Assert.assertEquals(result.getName(), "StopContainerTest");
            Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
            Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
            Assert.assertEquals(result.getActionCount(), 1L);
            Assert.assertEquals(result.getTestAction(0).getClass(), StopTestcontainersAction.class);
            Assert.assertTrue(result.getTestResult().isSuccess());

            Assert.assertFalse(busyBox.isRunning());
        }
    }
}
