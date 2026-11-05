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

package org.citrusframework.testcontainers.integration;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.citrusframework.testcontainers.TestcontainersActor;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.shaded.com.github.dockerjava.core.DefaultDockerClientConfig;
import org.testcontainers.shaded.com.github.dockerjava.core.DockerClientConfig;
import org.testcontainers.shaded.com.github.dockerjava.core.DockerClientImpl;

/**
 * Makes sure tests are executed only on hosts that meet prerequisites such as a Docker compatible engine installed and
 * clients able to connect.
 */
@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
@Isolated("Testcontainers ITs share the Docker client and the Ryuk resource reaper")
public abstract class AbstractTestcontainersIT {

    /** Docker connection state, checked before each integration test class */
    private static volatile boolean disabled = false;

    @BeforeAll
    static void checkDockerEnvironment() {
        disabled = new TestcontainersActor().isDisabled();
    }

    @BeforeEach
    void assumeDockerAvailable() {
        Assumptions.assumeTrue(!disabled, "Skipping test execution as no proper Docker environment is available on host system!");
    }

    protected DockerClient createDockerClient() {
        DockerClientConfig clientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        return DockerClientImpl.getInstance(clientConfig,
                new ApacheDockerHttpClient.Builder().dockerHost(clientConfig.getDockerHost()).build()
        );
    }
}
