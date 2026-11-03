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

package org.citrusframework.docker.config.xml;

import java.util.Map;

import org.citrusframework.docker.client.DockerClient;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DockerClientParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testDockerClientParser() {
        Map<String, DockerClient> clients = beanDefinitionContext.getBeansOfType(DockerClient.class);

        Assertions.assertEquals(2, clients.size());

        // 1st client
        DockerClient dockerClient = clients.get("dockerClient1");
        Assertions.assertNotNull(dockerClient.getEndpointConfiguration().getDockerClient());

        // 2nd client
        dockerClient = clients.get("dockerClient2");
        Assertions.assertNotNull(dockerClient.getEndpointConfiguration().getDockerClient());
        Assertions.assertEquals("tcp://localhost:2376", dockerClient.getEndpointConfiguration().getDockerClientConfig().getDockerHost().toString());
        Assertions.assertEquals("v1.19", dockerClient.getEndpointConfiguration().getDockerClientConfig().getApiVersion().asWebPathPart());
        Assertions.assertEquals("user", dockerClient.getEndpointConfiguration().getDockerClientConfig().getRegistryUsername());
        Assertions.assertEquals("s!cr!t", dockerClient.getEndpointConfiguration().getDockerClientConfig().getRegistryPassword());
        Assertions.assertEquals("user@foo.bar", dockerClient.getEndpointConfiguration().getDockerClientConfig().getRegistryEmail());
        Assertions.assertEquals("https://index.docker.io/v1/", dockerClient.getEndpointConfiguration().getDockerClientConfig().getRegistryUrl());
        Assertions.assertEquals("/path/to/some/config/directory", ((DefaultDockerClientConfig)dockerClient.getEndpointConfiguration().getDockerClientConfig()).getDockerConfigPath());
    }
}
