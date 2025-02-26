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

package org.citrusframework.agent.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.InternetProtocol;
import com.github.dockerjava.api.model.Mount;
import com.github.dockerjava.api.model.MountType;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Updatable;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.citrusframework.agent.plugin.config.DockerConfiguration;
import org.citrusframework.agent.plugin.config.JBangConfiguration;
import org.citrusframework.agent.plugin.config.KubernetesConfiguration;
import org.citrusframework.agent.plugin.config.ServerConfiguration;
import org.citrusframework.agent.plugin.kubernetes.KubernetesManifestHelper;
import org.citrusframework.jbang.CitrusJBang;
import org.citrusframework.jbang.ProcessAndOutput;
import org.citrusframework.util.StringUtils;

/**
 * Creates a Kubernetes deployment of the Citrus agent server application.
 */
@Mojo(name = "start", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.TEST)
public class StartMojo extends AbstractAgentMojo {

    @Parameter(property = "citrus.agent.skip.start", defaultValue = "false")
    protected boolean skipStart;

    @Parameter
    private KubernetesConfiguration kubernetes;

    @Parameter
    private DockerConfiguration docker;

    @Parameter
    private JBangConfiguration jbang;

    @Override
    public void doExecute() throws MojoExecutionException {
        if (skipStart) {
            getLog().info("Citrus agent stop is skipped.");
            return;
        }

        if (getKubernetes().isEnabled()) {
            startOnKubernetes();
        } else if (getDocker().isEnabled()) {
            startOnDocker();
        } else if (getJBang().isEnabled()) {
            startOnJBang();
        }
    }

    private void startOnKubernetes() throws MojoExecutionException {
        try (final KubernetesClient k8s = getKubernetes().getKubernetesClient()) {

            String ns = getKubernetes().getNamespace(k8s, getLog());

            k8s.configMaps()
                    .inNamespace(ns)
                    .resource(KubernetesManifestHelper.createTestSourceConfig(getServer().getName(), getTestJar()))
                    .createOr(Updatable::update);

            if (!serviceExists(k8s, ns)) {
                k8s.resourceList(KubernetesManifestHelper.createDeploymentManifest(getServer().getName(), getKubernetes().getImage().getImage()))
                        .inNamespace(ns)
                        .create();
                getLog().info("Started Citrus agent on the Kubernetes platform: %s (%s)".formatted(getServer().getName(), getKubernetes().getImage().getImage()));
            }
        }
    }

    private void startOnDocker() throws MojoExecutionException {
        try (final DockerClient dockerClient = getDocker().getDockerClient()) {
            List<String> env = new ArrayList<>();
            env.add("CITRUS_AGENT_TEST_JAR=/deployments/resources/%s-tests.jar".formatted(getServer().getName()));
            env.add("CITRUS_AGENT_SKIP_TESTS=true");
            env.add("CITRUS_AGENT_SERVER_PORT=8080");

            int port = Integer.parseInt(Optional.ofNullable(getServer().getLocalPort()).orElse(ServerConfiguration.AGENT_SERVER_PORT_DEFAULT));
            ExposedPort exposedPort = new ExposedPort(8080, InternetProtocol.TCP);
            CreateContainerResponse response = dockerClient.createContainerCmd(getDocker().getImage().getImage())
                    .withName(getServer().getName())
                    .withEnv(env)
                    .withExposedPorts(exposedPort)
                    .withHostConfig(HostConfig.newHostConfig()
                            .withMounts(Collections.singletonList(new Mount()
                                    .withType(MountType.BIND)
                                    .withReadOnly(true)
                                    .withSource(getTestJar().toAbsolutePath().toString())
                                    .withTarget("/deployments/resources/%s-tests.jar".formatted(getServer().getName()))))
                            .withPortBindings(new PortBinding(Ports.Binding.bindIpAndPort("127.0.0.1", port), exposedPort)))
                    .exec();

            dockerClient.startContainerCmd(response.getId()).exec();

            getLog().info("Started Citrus agent as Docker container with name %s (%s)".formatted(getDocker().getImage().getImage(), response.getId()));
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to start Citrus agent Docker container", e);
        }
    }

    private void startOnJBang() throws MojoExecutionException {
        CitrusJBang citrus = getJBang().getCitrusJBang()
                .withEnv("CITRUS_AGENT_SERVER_PORT", Optional.ofNullable(getServer().getLocalPort())
                        .orElse(ServerConfiguration.AGENT_SERVER_PORT_DEFAULT))
                .withEnv("CITRUS_AGENT_TEST_JAR", getTestJar().getFileName().toString())
                .withEnv("CITRUS_AGENT_SKIP_TESTS", "true")
                .addToClasspath(getTestJar().getFileName().toString())
                .workingDir(getTestJar().getParent().toAbsolutePath());

        Long pid = Optional.of(citrus.get(getServer().getName()).getOrDefault("PID", ""))
                .filter(StringUtils::hasText)
                .map(Long::parseLong)
                .orElse(0L);

        if (pid > 0) {
            getLog().info("Using existing Citrus agent JBang process %s (pid: %s)".formatted(getServer().getName(), pid));
        } else {
            ProcessAndOutput pao = citrus.agent().dumpOutput(getJBang().isDumpOutput()).start();
            getLog().info("Started Citrus agent as JBang process %s (pid: %s)".formatted(getServer().getName(), pao.getProcessId()));
        }
    }

    /**
     * Check if Citrus agent service already exists in the given namespace.
     */
    private boolean serviceExists(KubernetesClient k8s, String ns) {
        return k8s.services()
                .inNamespace(ns)
                .withName(getServer().getName())
                .get() != null;
    }

    private Path getTestJar() throws MojoExecutionException {
        return project.getAttachedArtifacts()
                .stream()
                .filter(Artifact::hasClassifier)
                .filter(artifact -> "tests".equals(artifact.getClassifier()))
                .map(Artifact::getFile)
                .map(File::toPath)
                .findFirst()
                .orElseThrow(() -> new MojoExecutionException("Failed to retrieve test jar"));
    }

    public JBangConfiguration getJBang() {
        if (jbang == null) {
            jbang = new JBangConfiguration();
        }

        return jbang;
    }

    public KubernetesConfiguration getKubernetes() {
        if (kubernetes == null) {
            kubernetes = new KubernetesConfiguration();
        }

        return kubernetes;
    }

    public DockerConfiguration getDocker() {
        if (docker == null) {
            docker = new DockerConfiguration();
        }

        return docker;
    }
}
