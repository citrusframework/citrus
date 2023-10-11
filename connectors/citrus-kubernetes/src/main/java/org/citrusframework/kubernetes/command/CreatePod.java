/*
 * Copyright 2006-2017 the original author or authors.
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

package org.citrusframework.kubernetes.command;

import io.fabric8.kubernetes.api.model.ContainerFluent;
import io.fabric8.kubernetes.api.model.ContainerPortFluent;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodFluent;
import io.fabric8.kubernetes.api.model.PodSpecFluent;
import io.fabric8.kubernetes.client.dsl.ClientMixedOperation;
import io.fabric8.kubernetes.client.dsl.ClientResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.citrusframework.kubernetes.message.KubernetesMessageHeaders;
import org.citrusframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class CreatePod extends AbstractCreateCommand<Pod, DoneablePod, CreatePod> {

    /** Docker image name */
    private String image;
    private String pullPolicy = "IfNotPresent";

    /** Container spec */
    private String containerName;
    private String containerCommand;
    private String containerPort;
    private String protocol = "TCP";
    private String restartPolicy = "Always";

    /**
     * Default constructor initializing the command name.
     */
    public CreatePod() {
        super("pod");
    }

    @Override
    protected ClientMixedOperation<Pod, ? extends KubernetesResourceList, DoneablePod, ? extends ClientResource<Pod, DoneablePod>> operation(KubernetesClient kubernetesClient, TestContext context) {
        return kubernetesClient.getClient().pods();
    }

    @Override
    protected DoneablePod specify(DoneablePod pod, TestContext context) {
        PodFluent.MetadataNested metadata = pod.editOrNewMetadata();

        if (getParameters().containsKey(KubernetesMessageHeaders.NAME)) {
            metadata.withName(getParameter(KubernetesMessageHeaders.NAME, context));
        }

        if (getParameters().containsKey(KubernetesMessageHeaders.LABEL)) {
            metadata.withLabels(getLabels(getParameters().get(KubernetesMessageHeaders.LABEL).toString(), context));
        }

        if (getParameters().containsKey(KubernetesMessageHeaders.NAMESPACE)) {
            metadata.withNamespace(getParameter(KubernetesMessageHeaders.NAMESPACE, context));
        }

        metadata.endMetadata();

        PodFluent.SpecNested spec = pod.editOrNewSpec();
        if (StringUtils.hasText(image)) {
            PodSpecFluent.ContainersNested containers = spec.addNewContainer();
            ContainerFluent container = containers.withImage(context.replaceDynamicContentInString(image));

            if (StringUtils.hasText(containerName)) {
                container.withName(context.replaceDynamicContentInString(containerName));
            }

            if (StringUtils.hasText(containerCommand)) {
                container.withCommand(context.replaceDynamicContentInString(containerCommand));
            }

            if (StringUtils.hasText(containerPort)) {
                ContainerFluent.PortsNested ports = container.addNewPort();
                ContainerPortFluent port = ports.withContainerPort(Integer.valueOf(context.replaceDynamicContentInString(containerPort)));

                port.withProtocol(context.replaceDynamicContentInString(protocol));

                ports.endPort();
            }

            containers.endContainer();
        }

        spec.withRestartPolicy(restartPolicy);

        spec.endSpec();

        return pod;
    }

    /**
     * Gets the image.
     *
     * @return
     */
    public String getImage() {
        return image;
    }

    /**
     * Sets the image.
     *
     * @param image
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * Gets the containerName.
     *
     * @return
     */
    public String getContainerName() {
        return containerName;
    }

    /**
     * Sets the containerName.
     *
     * @param containerName
     */
    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    /**
     * Gets the containerCommand.
     *
     * @return
     */
    public String getContainerCommand() {
        return containerCommand;
    }

    /**
     * Sets the containerCommand.
     *
     * @param containerCommand
     */
    public void setContainerCommand(String containerCommand) {
        this.containerCommand = containerCommand;
    }

    /**
     * Gets the pullPolicy.
     *
     * @return
     */
    public String getPullPolicy() {
        return pullPolicy;
    }

    /**
     * Sets the pullPolicy.
     *
     * @param pullPolicy
     */
    public void setPullPolicy(String pullPolicy) {
        this.pullPolicy = pullPolicy;
    }

    /**
     * Gets the containerPort.
     *
     * @return
     */
    public String getContainerPort() {
        return containerPort;
    }

    /**
     * Sets the containerPort.
     *
     * @param containerPort
     */
    public void setContainerPort(String containerPort) {
        this.containerPort = containerPort;
    }

    /**
     * Gets the protocol.
     *
     * @return
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Sets the protocol.
     *
     * @param protocol
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Gets the restartPolicy.
     *
     * @return
     */
    public String getRestartPolicy() {
        return restartPolicy;
    }

    /**
     * Sets the restartPolicy.
     *
     * @param restartPolicy
     */
    public void setRestartPolicy(String restartPolicy) {
        this.restartPolicy = restartPolicy;
    }

    /**
     * Gets the pod.
     *
     * @return
     */
    public Pod getPod() {
        return getResource();
    }

    /**
     * Sets the pod.
     *
     * @param pod
     */
    public CreatePod setPod(Pod pod) {
        setResource(pod);
        return this;
    }
}
