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

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class TestContainersSettingsTest {

    private static final String REGISTRY_MIRROR_ENABLED_PROPERTY = TESTCONTAINERS_PROPERTY_PREFIX + "registry.mirror.enabled";
    private static final String REGISTRY_MIRROR_PROPERTY = TESTCONTAINERS_PROPERTY_PREFIX + "registry.mirror";
    private static final String REGISTRY_PROPERTY = TESTCONTAINERS_PROPERTY_PREFIX + "registry";

    @AfterMethod
    public void cleanup() {
        System.clearProperty(REGISTRY_MIRROR_ENABLED_PROPERTY);
        System.clearProperty(REGISTRY_MIRROR_PROPERTY);
        System.clearProperty(REGISTRY_PROPERTY);
    }

    @Test
    public void shouldReturnImageUnchangedWhenMirrorDisabled() {
        Assert.assertEquals(TestContainersSettings.getDockerImageName("postgres"), "postgres");
        Assert.assertEquals(TestContainersSettings.getDockerImageName("apache/kafka"), "apache/kafka");
        Assert.assertEquals(TestContainersSettings.getDockerImageName("quay.io/strimzi/kafka"), "quay.io/strimzi/kafka");
    }

    @Test
    public void shouldApplyMirrorToDockerHubImages() {
        System.setProperty(REGISTRY_MIRROR_ENABLED_PROPERTY, "true");

        Assert.assertEquals(TestContainersSettings.getDockerImageName("postgres"), "mirror.gcr.io/postgres");
        Assert.assertEquals(TestContainersSettings.getDockerImageName("apache/kafka"), "mirror.gcr.io/apache/kafka");
        Assert.assertEquals(TestContainersSettings.getDockerImageName("confluentinc/cp-kafka"), "mirror.gcr.io/confluentinc/cp-kafka");
    }

    @Test
    public void shouldNotApplyMirrorToAlternateRegistryImages() {
        System.setProperty(REGISTRY_MIRROR_ENABLED_PROPERTY, "true");

        Assert.assertEquals(TestContainersSettings.getDockerImageName("quay.io/strimzi/kafka"), "quay.io/strimzi/kafka");
        Assert.assertEquals(TestContainersSettings.getDockerImageName("ghcr.io/some/image"), "ghcr.io/some/image");
        Assert.assertEquals(TestContainersSettings.getDockerImageName("localhost:5000/myimage"), "localhost:5000/myimage");
        Assert.assertEquals(TestContainersSettings.getDockerImageName("registry.example.com/image"), "registry.example.com/image");
    }

    @Test
    public void shouldApplyCustomMirror() {
        System.setProperty(REGISTRY_MIRROR_ENABLED_PROPERTY, "true");
        System.setProperty(REGISTRY_MIRROR_PROPERTY, "my-mirror.example.com");

        Assert.assertEquals(TestContainersSettings.getDockerImageName("postgres"), "my-mirror.example.com/postgres");
        Assert.assertEquals(TestContainersSettings.getDockerImageName("apache/kafka"), "my-mirror.example.com/apache/kafka");
        Assert.assertEquals(TestContainersSettings.getDockerImageName("quay.io/strimzi/kafka"), "quay.io/strimzi/kafka");
    }

    @Test
    public void shouldApplyCustomRegistryToDockerHubImages() {
        System.setProperty(REGISTRY_PROPERTY, "my-registry.example.com");

        Assert.assertEquals(TestContainersSettings.getDockerImageName("postgres"), "my-registry.example.com/postgres");
        Assert.assertEquals(TestContainersSettings.getDockerImageName("apache/kafka"), "my-registry.example.com/apache/kafka");
        Assert.assertEquals(TestContainersSettings.getDockerImageName("quay.io/strimzi/kafka"), "quay.io/strimzi/kafka");
    }
}
