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

package org.citrusframework.docker.config.annotation;

import java.util.Map;

import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.docker.client.DockerClient;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DockerClientConfigParserTest extends AbstractUnitTest {

    @CitrusEndpoint(name = "dockerClient1")
    @DockerClientConfig()
    private DockerClient dockerClient1;

    @CitrusEndpoint
    @DockerClientConfig(url = "tcp://localhost:2376",
            version="1.19",
            username="user",
            password="s!cr!t",
            email="user@foo.bar",
            registry="https://index.docker.io/v1/",
            certPath="/path/to/some/cert/directory",
            configPath="/path/to/some/config/directory")
    private DockerClient dockerClient2;

    @Test
    public void testDockerClientParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st client
        Assertions.assertNotNull(dockerClient1.getEndpointConfiguration().getDockerClient());

        // 2nd client
        Assertions.assertNotNull(dockerClient2.getEndpointConfiguration().getDockerClient());
        Assertions.assertEquals("tcp://localhost:2376", dockerClient2.getEndpointConfiguration().getDockerClientConfig().getDockerHost().toString());
        Assertions.assertEquals("v1.19", dockerClient2.getEndpointConfiguration().getDockerClientConfig().getApiVersion().asWebPathPart());
        Assertions.assertEquals("user", dockerClient2.getEndpointConfiguration().getDockerClientConfig().getRegistryUsername());
        Assertions.assertEquals("s!cr!t", dockerClient2.getEndpointConfiguration().getDockerClientConfig().getRegistryPassword());
        Assertions.assertEquals("user@foo.bar", dockerClient2.getEndpointConfiguration().getDockerClientConfig().getRegistryEmail());
        Assertions.assertEquals("https://index.docker.io/v1/", dockerClient2.getEndpointConfiguration().getDockerClientConfig().getRegistryUrl());
        Assertions.assertEquals("/path/to/some/config/directory", ((DefaultDockerClientConfig)dockerClient2.getEndpointConfiguration().getDockerClientConfig()).getDockerConfigPath());
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser<?, ?>> validators = AnnotationConfigParser.lookup();
        Assertions.assertEquals(3L, validators.size());
        Assertions.assertNotNull(validators.get("direct.async"));
        Assertions.assertEquals(DirectEndpointConfigParser.class, validators.get("direct.async").getClass());
        Assertions.assertNotNull(validators.get("direct.sync"));
        Assertions.assertEquals(DirectSyncEndpointConfigParser.class, validators.get("direct.sync").getClass());
        Assertions.assertNotNull(validators.get("docker.client"));
        Assertions.assertEquals(DockerClientConfigParser.class, validators.get("docker.client").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(AnnotationConfigParser.lookup("docker.client").isPresent());
    }
}
