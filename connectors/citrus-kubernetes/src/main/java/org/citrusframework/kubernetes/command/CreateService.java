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

import io.fabric8.kubernetes.api.model.DoneableService;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceFluent;
import io.fabric8.kubernetes.api.model.ServicePortFluent;
import io.fabric8.kubernetes.api.model.ServiceSpecFluent;
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
public class CreateService extends AbstractCreateCommand<Service, DoneableService, CreateService> {

    private String selector;

    private String port;
    private String targetPort;
    private String nodePort;
    private String protocol = "TCP";

    /**
     * Default constructor initializing the command name.
     */
    public CreateService() {
        super("pod");
    }

    @Override
    protected ClientMixedOperation<Service, ? extends KubernetesResourceList, DoneableService, ? extends ClientResource<Service, DoneableService>> operation(KubernetesClient kubernetesClient, TestContext context) {
        return kubernetesClient.getClient().services();
    }

    @Override
    protected DoneableService specify(DoneableService service, TestContext context) {
        ServiceFluent.MetadataNested metadata = service.editOrNewMetadata();

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

        ServiceFluent.SpecNested spec = service.editOrNewSpec();
        if (StringUtils.hasText(selector)) {
            spec.withSelector(getLabels(selector, context));
        }

        if (StringUtils.hasText(port)) {
            ServiceSpecFluent.PortsNested ports = spec.addNewPort();
            ServicePortFluent container = ports.withPort(Integer.valueOf(context.replaceDynamicContentInString(port)));

            if (StringUtils.hasText(targetPort)) {
                container.withNewTargetPort(Integer.valueOf(context.replaceDynamicContentInString(targetPort)));
            } else {
                container.withNewTargetPort();
            }

            if (StringUtils.hasText(nodePort)) {
                container.withNodePort(Integer.valueOf(context.replaceDynamicContentInString(nodePort)));
            }

            container.withProtocol(context.replaceDynamicContentInString(protocol));

            ports.endPort();
        }

        spec.endSpec();

        return service;
    }

    /**
     * Gets the selector.
     *
     * @return
     */
    public String getSelector() {
        return selector;
    }

    /**
     * Sets the selector.
     *
     * @param selector
     */
    public void setSelector(String selector) {
        this.selector = selector;
    }

    /**
     * Gets the port.
     *
     * @return
     */
    public String getPort() {
        return port;
    }

    /**
     * Sets the port.
     *
     * @param port
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * Gets the targetPort.
     *
     * @return
     */
    public String getTargetPort() {
        return targetPort;
    }

    /**
     * Sets the targetPort.
     *
     * @param targetPort
     */
    public void setTargetPort(String targetPort) {
        this.targetPort = targetPort;
    }

    /**
     * Gets the nodePort.
     *
     * @return
     */
    public String getNodePort() {
        return nodePort;
    }

    /**
     * Sets the nodePort.
     *
     * @param nodePort
     */
    public void setNodePort(String nodePort) {
        this.nodePort = nodePort;
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
     * Gets the service.
     *
     * @return
     */
    public Service getService() {
        return getResource();
    }

    /**
     * Sets the service.
     *
     * @param service
     */
    public CreateService setService(Service service) {
        setResource(service);
        return this;
    }
}
