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

package org.citrusframework.kubernetes.actions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Updatable;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jbang.CitrusJBang;
import org.citrusframework.kubernetes.CitrusAgentSettings;
import org.citrusframework.kubernetes.KubernetesSettings;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.StringUtils;

import static org.citrusframework.kubernetes.actions.KubernetesActionBuilder.kubernetes;

/**
 * Action connects the test to a Citrus agent running on the Kubernetes platform.
 * The test action deploys the agent to the given Kubernetes namespace and connects to the agent Pod via port forwarding.
 * The action then exposes a Http client to the Citrus context so other test actions can access the agent to run test actions within the Kubernetes cluster.
 */
public class AgentConnectAction extends ServiceConnectAction {

    public static final String KUBERNETES_LABEL_NAME = "app.kubernetes.io/name";
    public static final String KUBERNETES_LABEL_MANAGED_BY = "app.kubernetes.io/managed-by";

    private final String agentName;
    private final String imageRegistry;
    private final String imageName;
    private final String imageTag;
    private final String testJar;

    public AgentConnectAction(Builder builder) {
        super("agent-connect", builder);

        this.agentName = builder.agentName;
        this.imageRegistry = builder.imageRegistry;
        this.imageName = builder.imageName;
        this.imageTag = builder.imageTag;
        this.testJar = builder.testJar;
    }

    @Override
    public void doExecute(TestContext context) {
        String agent = context.replaceDynamicContentInString(agentName);
        logger.info("Creating Kubernetes agent '{}'", agent);

        Path testJarPath = null;
        if (testJar != null) {
            testJarPath = Resources.create(testJar).getFile().toPath();
        }

        if (KubernetesSettings.isLocal()) {
            CitrusJBang jbang = new CitrusJBang()
                    .withEnv("CITRUS_AGENT_SERVER_PORT", Optional.ofNullable(localPort)
                            .orElse(CitrusAgentSettings.getServerPort()))
                    .withEnv("CITRUS_AGENT_SKIP_TESTS", "true");

            if (testJarPath != null) {
                jbang.withEnv("CITRUS_AGENT_TEST_JAR", testJarPath.getFileName().toString());
                jbang.addToClasspath(testJarPath.getFileName().toString());
                jbang.workingDir(testJarPath.getParent().toAbsolutePath());
            }

            jbang.agent().start();
        } else {
            getKubernetesClient().configMaps()
                    .inNamespace(namespace(context))
                    .resource(createTestSourceConfig(agent, testJarPath))
                    .createOr(Updatable::update);

            boolean autoCreate = !serviceExists(getKubernetesClient(), agent, namespace(context));
            if (autoCreate) {
                getKubernetesClient().resourceList(createDeploymentManifest(agent, getImage(), port, context))
                        .inNamespace(namespace(context))
                        .create();
            }

            if (autoCreate && isAutoRemoveResources()) {
                context.doFinally(kubernetes().client(getKubernetesClient())
                        .agent()
                        .disconnect(agent)
                        .inNamespace(getNamespace()));
            }
        }

        logger.info("Kubernetes agent '{}' created successfully", agent);
        super.doExecute(context);
    }

    /**
     * Check if Citrus agent service already exists in the given namespace.
     */
    private static boolean serviceExists(KubernetesClient k8s, String agentName, String namespace) {
        return k8s.services()
                .inNamespace(namespace)
                .withName(agentName)
                .get() != null;
    }

    private static ConfigMap createTestSourceConfig(String agentName, Path testJar) {
        ConfigMapBuilder cmb = new ConfigMapBuilder()
                .withNewMetadata()
                    .withName(agentName + "-resources")
                    .addToLabels("app", "citrus")
                    .addToLabels(KUBERNETES_LABEL_NAME, agentName)
                    .addToLabels(KUBERNETES_LABEL_MANAGED_BY, "citrus")
                .endMetadata();

        if (testJar != null) {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Files.copy(testJar, out);
                cmb.withBinaryData(Collections.singletonMap(agentName + "-tests.jar",
                        Base64.getEncoder().encodeToString(out.toByteArray())));
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to read test jar artifact", e);
            }
        }

        return cmb.build();
    }

    private static Collection<? extends HasMetadata> createDeploymentManifest(String agentName, String image, String port, TestContext context) {
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
                                        .withContainerPort(Integer.parseInt(context.replaceDynamicContentInString(port)))
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

    private String getImage() {
        if (StringUtils.hasText(imageRegistry)) {
            return "%s/%s:%s".formatted(imageRegistry, imageName, imageTag);
        }

        return "%s:%s".formatted(imageName, imageTag);
    }

    /**
     * Action builder.
     */
    public static class Builder extends ServiceConnectAction.Builder {

        private String agentName = CitrusAgentSettings.getAgentName();
        private String imageName = CitrusAgentSettings.getImage();
        private String imageRegistry = "";
        private String imageTag = CitrusAgentSettings.getVersion();
        private String testJar;

        public Builder client(KubernetesClient kubernetesClient) {
            super.client(kubernetesClient);
            return this;
        }

        public Builder service(String name) {
            agent(name);
            return this;
        }

        public Builder agent(String agentName) {
            this.agentName = agentName;
            return this;
        }

        public Builder image(String imageName) {
            if (imageName.contains(":")) {
                String[] tokens = imageName.split(":");
                return image(tokens[0], tokens[1]);
            }

            this.imageName = imageName;
            return this;
        }

        public Builder image(String imageName, String version) {
            this.imageName = imageName;
            this.imageTag = version;
            return this;
        }

        public Builder registry(String imageRegistry) {
            this.imageRegistry = imageRegistry;
            return this;
        }

        public Builder testJar(String testJar) {
            this.testJar = testJar;
            return this;
        }

        @Override
        public AgentConnectAction doBuild() {
            if (localPort == null) {
                localPort(CitrusAgentSettings.getServerPort());
            }

            super.service(agentName);
            super.doBuild();

            return new AgentConnectAction(this);
        }
    }
}
