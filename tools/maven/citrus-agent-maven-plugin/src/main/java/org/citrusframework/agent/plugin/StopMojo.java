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

import java.io.IOException;

import com.github.dockerjava.api.DockerClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.citrusframework.agent.plugin.config.DockerConfiguration;
import org.citrusframework.agent.plugin.config.JBangConfiguration;
import org.citrusframework.agent.plugin.config.KubernetesConfiguration;
import org.citrusframework.jbang.CitrusJBang;

/**
 * Deletes Kubernetes deployment and all resources owned by the Citrus agent server application.
 */
@Mojo(name = "stop", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.TEST)
public class StopMojo extends AbstractAgentMojo {

    @Parameter(property = "citrus.agent.skip.stop", defaultValue = "false")
    protected boolean skipStop;

    @Parameter
    private KubernetesConfiguration kubernetes;

    @Parameter
    private DockerConfiguration docker;

    @Parameter
    private JBangConfiguration jbang;

    @Override
    public void doExecute() throws MojoExecutionException {
        if (skipStop) {
            getLog().info("Citrus agent stop is skipped.");
            return;
        }

        if (getKubernetes().isEnabled()) {
            stopOnKubernetes();
        } else if (getDocker().isEnabled()) {
            stopOnDocker();
        } else if (getJBang().isEnabled()) {
            stopOnJBang();
        }
    }

    private void stopOnKubernetes() {
        try (final KubernetesClient k8s = getKubernetes().getKubernetesClient()) {
            String ns = getKubernetes().getNamespace(k8s, getLog());

            k8s.services()
                    .inNamespace(ns)
                    .withLabel("app", "citrus")
                    .delete();

            k8s.apps().deployments()
                    .inNamespace(ns)
                    .withLabel("app", "citrus")
                    .delete();

            k8s.configMaps()
                    .inNamespace(ns)
                    .withLabel("app", "citrus")
                    .delete();

            getLog().info("Stopped Citrus agent Kubernetes service %s".formatted(getServer().getName()));
        }
    }

    private void stopOnDocker() throws MojoExecutionException {
        try (final DockerClient dockerClient = getDocker().getDockerClient()) {
            dockerClient.stopContainerCmd(getServer().getName()).exec();
            dockerClient.removeContainerCmd(getServer().getName()).exec();

            getLog().info("Stopped Citrus agent Docker container %s".formatted(getServer().getName()));
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to stop Citrus agent Docker container", e);
        }
    }

    private void stopOnJBang() {
        CitrusJBang citrus = getJBang().getCitrusJBang();
        Long pid = citrus.agent().stop(getServer().getName());

        getLog().info("Stopped Citrus agent JBang process %s (pid: %d)".formatted(getServer().getName(), pid));
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
