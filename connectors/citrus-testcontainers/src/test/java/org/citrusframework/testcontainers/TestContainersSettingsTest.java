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

package org.citrusframework.testcontainers;

import static org.citrusframework.testcontainers.TestContainersSettings.TESTCONTAINERS_PROPERTY_PREFIX;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class TestContainersSettingsTest {

    private static final String REGISTRY_MIRROR_ENABLED_PROPERTY = TESTCONTAINERS_PROPERTY_PREFIX + "registry.mirror.enabled";
    private static final String REGISTRY_MIRROR_PROPERTY = TESTCONTAINERS_PROPERTY_PREFIX + "registry.mirror";
    private static final String REGISTRY_PROPERTY = TESTCONTAINERS_PROPERTY_PREFIX + "registry";

    @AfterEach
    public void cleanup() {
        System.clearProperty(REGISTRY_MIRROR_ENABLED_PROPERTY);
        System.clearProperty(REGISTRY_MIRROR_PROPERTY);
        System.clearProperty(REGISTRY_PROPERTY);
    }

    @Test
    public void shouldReturnImageUnchangedWhenMirrorDisabled() {
        Assertions.assertEquals("postgres", TestContainersSettings.getDockerImageName("postgres"));
        Assertions.assertEquals("apache/kafka", TestContainersSettings.getDockerImageName("apache/kafka"));
        Assertions.assertEquals("quay.io/strimzi/kafka", TestContainersSettings.getDockerImageName("quay.io/strimzi/kafka"));
    }

    @Test
    public void shouldApplyMirrorToDockerHubImages() {
        System.setProperty(REGISTRY_MIRROR_ENABLED_PROPERTY, "true");

        Assertions.assertEquals("mirror.gcr.io/postgres", TestContainersSettings.getDockerImageName("postgres"));
        Assertions.assertEquals("mirror.gcr.io/apache/kafka", TestContainersSettings.getDockerImageName("apache/kafka"));
        Assertions.assertEquals("mirror.gcr.io/confluentinc/cp-kafka", TestContainersSettings.getDockerImageName("confluentinc/cp-kafka"));
    }

    @Test
    public void shouldNotApplyMirrorToAlternateRegistryImages() {
        System.setProperty(REGISTRY_MIRROR_ENABLED_PROPERTY, "true");

        Assertions.assertEquals("quay.io/strimzi/kafka", TestContainersSettings.getDockerImageName("quay.io/strimzi/kafka"));
        Assertions.assertEquals("ghcr.io/some/image", TestContainersSettings.getDockerImageName("ghcr.io/some/image"));
        Assertions.assertEquals("localhost:5000/myimage", TestContainersSettings.getDockerImageName("localhost:5000/myimage"));
        Assertions.assertEquals("registry.example.com/image", TestContainersSettings.getDockerImageName("registry.example.com/image"));
    }

    @Test
    public void shouldApplyCustomMirror() {
        System.setProperty(REGISTRY_MIRROR_ENABLED_PROPERTY, "true");
        System.setProperty(REGISTRY_MIRROR_PROPERTY, "my-mirror.example.com");

        Assertions.assertEquals("my-mirror.example.com/postgres", TestContainersSettings.getDockerImageName("postgres"));
        Assertions.assertEquals("my-mirror.example.com/apache/kafka", TestContainersSettings.getDockerImageName("apache/kafka"));
        Assertions.assertEquals("quay.io/strimzi/kafka", TestContainersSettings.getDockerImageName("quay.io/strimzi/kafka"));
    }

    @Test
    public void shouldApplyCustomRegistryToDockerHubImages() {
        System.setProperty(REGISTRY_PROPERTY, "my-registry.example.com");

        Assertions.assertEquals("my-registry.example.com/postgres", TestContainersSettings.getDockerImageName("postgres"));
        Assertions.assertEquals("my-registry.example.com/apache/kafka", TestContainersSettings.getDockerImageName("apache/kafka"));
        Assertions.assertEquals("quay.io/strimzi/kafka", TestContainersSettings.getDockerImageName("quay.io/strimzi/kafka"));
    }
}
