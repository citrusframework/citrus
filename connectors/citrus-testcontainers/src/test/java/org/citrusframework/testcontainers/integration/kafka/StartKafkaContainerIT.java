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

package org.citrusframework.testcontainers.integration.kafka;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.testcontainers.integration.AbstractTestcontainersIT;
import org.citrusframework.testcontainers.kafka.KafkaImplementation;
import org.citrusframework.testcontainers.kafka.KafkaSettings;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(singleThreaded = true)
public class StartKafkaContainerIT extends AbstractTestcontainersIT implements TestActionSupport {

    @CitrusTest
    public void shouldStartContainer() {
        given(doFinally().actions(testcontainers().stop()
                .containerName(KafkaSettings.CONTAINER_NAME_DEFAULT)));

        when(testcontainers()
                .kafka()
                .start());

        then(this::verifyContainer);
    }

    @CitrusTest
    public void shouldAutoCreateTopics() {
        given(doFinally().actions(testcontainers().stop()
                .containerName(KafkaSettings.CONTAINER_NAME_DEFAULT)));

        when(testcontainers()
                .kafka()
                .start()
                .implementation(KafkaImplementation.APACHE_NATIVE.name())
                .topics("test-topic-1", "test-topic-2"));

        then(this::verifyTopics);
    }

    private void verifyContainer(TestContext context) {
        try (DockerClient dockerClient = createDockerClient()) {
            InspectContainerResponse response = dockerClient.inspectContainerCmd(context.getVariable("${CITRUS_TESTCONTAINERS_KAFKA_CONTAINER_ID}"))
                    .exec();

            Assert.assertNotNull(response.getState().getRunning());
            Assert.assertTrue(response.getState().getRunning());
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to verify Docker container", e);
        }
    }

    private void verifyTopics(TestContext context) {
        String bootstrapServers = context.getVariable("${CITRUS_TESTCONTAINERS_KAFKA_BOOTSTRAP_SERVERS}");

        try (Admin adminClient = Admin.create(Collections.singletonMap(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers))) {
            Set<String> topics = adminClient.listTopics().names().get();
            Assert.assertTrue(topics.contains("test-topic-1"), "Topic 'test-topic-1' should exist");
            Assert.assertTrue(topics.contains("test-topic-2"), "Topic 'test-topic-2' should exist");
        } catch (ExecutionException | InterruptedException e) {
            throw new CitrusRuntimeException("Failed to verify Kafka topics", e);
        }
    }
}
