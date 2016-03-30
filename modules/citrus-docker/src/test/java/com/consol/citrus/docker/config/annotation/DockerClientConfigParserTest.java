/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.docker.config.annotation;

import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.context.SpringBeanReferenceResolver;
import com.consol.citrus.docker.client.DockerClient;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.github.dockerjava.core.LocalDirectorySSLConfig;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class DockerClientConfigParserTest extends AbstractTestNGUnitTest {

    @CitrusEndpoint(name = "dockerClient1")
    @DockerClientConfig()
    private DockerClient dockerClient1;

    @CitrusEndpoint
    @DockerClientConfig(url = "http://localhost:2376",
            version="1.19",
            username="user",
            password="s!cr!t",
            email="user@consol.de",
            serverAddress="https://index.docker.io/v1/",
            certPath="/path/to/some/cert/directory",
            configPath="/path/to/some/config/directory")
    private DockerClient dockerClient2;

    @Autowired
    private SpringBeanReferenceResolver referenceResolver;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);
        referenceResolver.setApplicationContext(applicationContext);
    }

    @Test
    public void testDockerClientParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st client
        Assert.assertNotNull(dockerClient1.getEndpointConfiguration().getDockerClient());

        // 2nd client
        Assert.assertNotNull(dockerClient2.getEndpointConfiguration().getDockerClient());
        Assert.assertEquals(dockerClient2.getEndpointConfiguration().getDockerClientConfig().getUri().toString(), "http://localhost:2376");
        Assert.assertEquals(dockerClient2.getEndpointConfiguration().getDockerClientConfig().getVersion().asWebPathPart(), "v1.19");
        Assert.assertEquals(dockerClient2.getEndpointConfiguration().getDockerClientConfig().getUsername(), "user");
        Assert.assertEquals(dockerClient2.getEndpointConfiguration().getDockerClientConfig().getPassword(), "s!cr!t");
        Assert.assertEquals(dockerClient2.getEndpointConfiguration().getDockerClientConfig().getEmail(), "user@consol.de");
        Assert.assertEquals(dockerClient2.getEndpointConfiguration().getDockerClientConfig().getServerAddress(), "https://index.docker.io/v1/");
        Assert.assertEquals(((LocalDirectorySSLConfig)dockerClient2.getEndpointConfiguration().getDockerClientConfig().getSslConfig()).getDockerCertPath(), "/path/to/some/cert/directory");
        Assert.assertEquals(dockerClient2.getEndpointConfiguration().getDockerClientConfig().getDockerCfgPath(), "/path/to/some/config/directory");
    }
}
