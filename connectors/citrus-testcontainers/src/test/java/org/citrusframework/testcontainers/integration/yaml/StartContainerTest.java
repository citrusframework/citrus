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

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.model.Frame;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.testcontainers.actions.StartTestcontainersAction;
import org.citrusframework.yaml.YamlTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class StartContainerTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadTestcontainersActions() {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/testcontainers/yaml/start-container-test.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "StartContainerTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 1L);
        Assert.assertEquals(result.getTestAction(0).getClass(), StartTestcontainersAction.class);
        Assert.assertTrue(result.getTestResult().isSuccess());

        verifyContainerLogs(context);
    }

    private void verifyContainerLogs(TestContext context) {
        try (DockerClient dockerClient = createDockerClient()) {
            CompletableFuture<String> containerLogs = new CompletableFuture<>();
            dockerClient.logContainerCmd(context.getVariable("${CITRUS_TESTCONTAINERS_MY_CONTAINER_CONTAINER_ID}"))
                    .withStdOut(true)
                    .exec(new ResultCallbackTemplate<ResultCallback<Frame>, Frame>() {
                        @Override
                        public void onNext(Frame object) {
                            containerLogs.complete(new String(object.getPayload()).trim());
                        }
                    });

            Assert.assertEquals(containerLogs.get(5000L, TimeUnit.MILLISECONDS), "Hello World");
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to verify Docker container", e);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new CitrusRuntimeException("Unable to get logs from container", e);
        }
    }
}
