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

package org.citrusframework.agent.plugin.kubernetes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ConfigMapVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.LabelSelectorBuilder;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import org.apache.maven.plugin.MojoExecutionException;

public final class KubernetesManifestHelper {

    public static final String KUBERNETES_LABEL_NAME = "app.kubernetes.io/name";
    public static final String KUBERNETES_LABEL_MANAGED_BY = "app.kubernetes.io/managed-by";

    private KubernetesManifestHelper() {
        // prevent instantiation of utility class.
    }

    public static ConfigMap createTestSourceConfig(String agentName, Path testJar) throws MojoExecutionException {
        ConfigMapBuilder cm = new ConfigMapBuilder()
                .withNewMetadata()
                    .withName(agentName + "-resources")
                    .addToLabels("app", "citrus")
                    .addToLabels(KUBERNETES_LABEL_NAME, agentName)
                    .addToLabels(KUBERNETES_LABEL_MANAGED_BY, "citrus")
                .endMetadata();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Files.copy(testJar, out);
            cm.addToBinaryData(agentName + "-tests.jar", Base64.getEncoder().encodeToString(out.toByteArray()));
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to read test jar artifact", e);
        }

        return cm.build();
    }

    public static Collection<? extends HasMetadata> createDeploymentManifest(String agentName, String image) throws MojoExecutionException {
        List<HasMetadata> resources = new ArrayList<>();

        resources.add(new DeploymentBuilder()
                .withNewMetadata()
                    .withName(agentName)
                    .addToLabels("app", "citrus")
                    .addToLabels(KUBERNETES_LABEL_NAME, agentName)
                    .addToLabels(KUBERNETES_LABEL_MANAGED_BY, "citrus")
                .endMetadata()
                .withNewSpec()
                .withNewTemplate()
                    .withNewMetadata()
                        .addToLabels("app", "citrus")
                        .addToLabels(KUBERNETES_LABEL_NAME, agentName)
                        .addToLabels(KUBERNETES_LABEL_MANAGED_BY, "citrus")
                    .endMetadata()
                .editOrNewSpec()
                    .addToContainers(new ContainerBuilder()
                            .withName("citrus-agent")
                            .withImage(image)
                            .withImagePullPolicy("IfNotPresent")
                            .withEnv(new EnvVarBuilder()
                                    .withName("CITRUS_AGENT_TEST_JAR")
                                    .withValue("/deployments/resources/%s-tests.jar".formatted(agentName))
                                    .build(),
                                    new EnvVarBuilder()
                                    .withName("CITRUS_AGENT_SKIP_TESTS")
                                    .withValue("true")
                                    .build(),
                                    new EnvVarBuilder()
                                    .withName("CITRUS_AGENT_SERVER_PORT")
                                    .withValue("8080")
                                    .build())
                            .addToPorts(new ContainerPortBuilder()
                                    .withName("http")
                                    .withContainerPort(8080)
                                    .withProtocol("TCP")
                                    .build())
                            .addToVolumeMounts(new VolumeMountBuilder()
                                    .withName("resources")
                                    .withMountPath("/deployments/resources")
                                    .build())
                            .build())
                    .addToVolumes(new VolumeBuilder()
                            .withName("resources")
                            .withConfigMap(new ConfigMapVolumeSourceBuilder()
                                    .withName(agentName + "-resources")
                                    .build())
                            .build())
                .endSpec()
                .endTemplate()
                .withSelector(new LabelSelectorBuilder()
                        .withMatchLabels(Map.of(KUBERNETES_LABEL_NAME, agentName))
                        .build())
                .endSpec()
                .build());

        resources.add(new ServiceBuilder()
                .withNewMetadata()
                    .withName(agentName)
                    .addToLabels("app", "citrus")
                    .addToLabels(KUBERNETES_LABEL_NAME, agentName)
                    .addToLabels(KUBERNETES_LABEL_MANAGED_BY, "citrus")
                .endMetadata()
                .withNewSpec()
                    .withType("ClusterIP")
                    .withSelector(Map.of(KUBERNETES_LABEL_NAME, agentName))
                    .addToPorts(new ServicePortBuilder()
                            .withName("http")
                            .withPort(80)
                            .withTargetPort(new IntOrString(8080))
                            .withProtocol("TCP")
                            .build())
                .endSpec()
                .build());

        return resources;
    }
}
