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

package com.consol.citrus.docker.config.xml;

import com.consol.citrus.docker.client.DockerClient;
import com.consol.citrus.testng.AbstractBeanDefinitionParserTest;
import com.github.dockerjava.core.LocalDirectorySSLConfig;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

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
        Assert.assertEquals(dockerClient.getEndpointConfiguration().getDockerClientConfig().getUri().toString(), "http://localhost:2376");
        Assert.assertEquals(dockerClient.getEndpointConfiguration().getDockerClientConfig().getVersion().asWebPathPart(), "v1.19");
        Assert.assertEquals(dockerClient.getEndpointConfiguration().getDockerClientConfig().getUsername(), "user");
        Assert.assertEquals(dockerClient.getEndpointConfiguration().getDockerClientConfig().getPassword(), "s!cr!t");
        Assert.assertEquals(dockerClient.getEndpointConfiguration().getDockerClientConfig().getEmail(), "user@consol.de");
        Assert.assertEquals(dockerClient.getEndpointConfiguration().getDockerClientConfig().getServerAddress(), "https://index.docker.io/v1/");
        Assert.assertEquals(((LocalDirectorySSLConfig)dockerClient.getEndpointConfiguration().getDockerClientConfig().getSslConfig()).getDockerCertPath(), "/path/to/some/cert/directory");
        Assert.assertEquals(dockerClient.getEndpointConfiguration().getDockerClientConfig().getDockerCfgPath(), "/path/to/some/config/directory");
    }
}
