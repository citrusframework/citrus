/*
 * Copyright 2006-2015 the original author or authors.
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
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class DockerClientParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testDockerClientParser() {
        Map<String, DockerClient> clients = beanDefinitionContext.getBeansOfType(DockerClient.class);

        Assert.assertEquals(clients.size(), 2);

        // 1st client
        DockerClient dockerClient = clients.get("dockerClient1");
        Assert.assertNotNull(dockerClient.getEndpointConfiguration().getDockerClient());

        // 2nd client
        dockerClient = clients.get("dockerClient2");
        Assert.assertNotNull(dockerClient.getEndpointConfiguration().getDockerClient());
        Assert.assertEquals(dockerClient.getEndpointConfiguration().getDockerClientConfig().getDockerHost().toString(), "tcp://localhost:2376");
        Assert.assertEquals(dockerClient.getEndpointConfiguration().getDockerClientConfig().getApiVersion().asWebPathPart(), "v1.19");
        Assert.assertEquals(dockerClient.getEndpointConfiguration().getDockerClientConfig().getRegistryUsername(), "user");
        Assert.assertEquals(dockerClient.getEndpointConfiguration().getDockerClientConfig().getRegistryPassword(), "s!cr!t");
        Assert.assertEquals(dockerClient.getEndpointConfiguration().getDockerClientConfig().getRegistryEmail(), "user@foo.bar");
        Assert.assertEquals(dockerClient.getEndpointConfiguration().getDockerClientConfig().getRegistryUrl(), "https://index.docker.io/v1/");
        Assert.assertEquals(((DefaultDockerClientConfig)dockerClient.getEndpointConfiguration().getDockerClientConfig()).getDockerConfigPath(), "/path/to/some/config/directory");
    }
}
