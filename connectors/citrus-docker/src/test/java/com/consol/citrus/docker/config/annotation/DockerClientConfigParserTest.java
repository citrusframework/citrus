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

import java.util.Map;

import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.config.annotation.AnnotationConfigParser;
import com.consol.citrus.docker.client.DockerClient;
import com.consol.citrus.endpoint.direct.annotation.DirectEndpointConfigParser;
import com.consol.citrus.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import org.mockito.MockitoAnnotations;
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
    @DockerClientConfig(url = "tcp://localhost:2376",
            version="1.19",
            username="user",
            password="s!cr!t",
            email="user@consol.de",
            registry="https://index.docker.io/v1/",
            certPath="/path/to/some/cert/directory",
            configPath="/path/to/some/config/directory")
    private DockerClient dockerClient2;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testDockerClientParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st client
        Assert.assertNotNull(dockerClient1.getEndpointConfiguration().getDockerClient());

        // 2nd client
        Assert.assertNotNull(dockerClient2.getEndpointConfiguration().getDockerClient());
        Assert.assertEquals(dockerClient2.getEndpointConfiguration().getDockerClientConfig().getDockerHost().toString(), "tcp://localhost:2376");
        Assert.assertEquals(dockerClient2.getEndpointConfiguration().getDockerClientConfig().getApiVersion().asWebPathPart(), "v1.19");
        Assert.assertEquals(dockerClient2.getEndpointConfiguration().getDockerClientConfig().getRegistryUsername(), "user");
        Assert.assertEquals(dockerClient2.getEndpointConfiguration().getDockerClientConfig().getRegistryPassword(), "s!cr!t");
        Assert.assertEquals(dockerClient2.getEndpointConfiguration().getDockerClientConfig().getRegistryEmail(), "user@consol.de");
        Assert.assertEquals(dockerClient2.getEndpointConfiguration().getDockerClientConfig().getRegistryUrl(), "https://index.docker.io/v1/");
        Assert.assertEquals(((DefaultDockerClientConfig)dockerClient2.getEndpointConfiguration().getDockerClientConfig()).getDockerConfigPath(), "/path/to/some/config/directory");
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser> validators = AnnotationConfigParser.lookup();
        Assert.assertEquals(validators.size(), 3L);
        Assert.assertNotNull(validators.get("direct.async"));
        Assert.assertEquals(validators.get("direct.async").getClass(), DirectEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("direct.sync"));
        Assert.assertEquals(validators.get("direct.sync").getClass(), DirectSyncEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("docker.client"));
        Assert.assertEquals(validators.get("docker.client").getClass(), DockerClientConfigParser.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("docker.client").isPresent());
    }
}
