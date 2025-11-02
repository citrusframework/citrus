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

package org.citrusframework.kubernetes.xml;

import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActionContainerBuilder;
import org.citrusframework.TestActor;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kubernetes.KubernetesSettings;
import org.citrusframework.kubernetes.actions.AbstractKubernetesAction;
import org.citrusframework.kubernetes.actions.KubernetesAction;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

@XmlRootElement(name = "kubernetes")
public class Kubernetes implements TestActionBuilder<KubernetesAction>, ReferenceResolverAware {

    private AbstractKubernetesAction.Builder<?, ?> builder;

    private String description;
    private String actor;

    private String kubernetesClient;
    private String namespace;

    private boolean autoRemoveResources = KubernetesSettings.isAutoRemoveResources();

    private ReferenceResolver referenceResolver;

    @XmlElement
    public void setDescription(String value) {
        this.description = value;
    }

    @XmlAttribute
    public void setActor(String actor) {
        this.actor = actor;
    }

    @XmlAttribute
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @XmlAttribute(name = "auto-remove")
    public void setAutoRemove(boolean autoRemoveResources) {
        this.autoRemoveResources = autoRemoveResources;
    }

    @XmlAttribute
    public void setClient(String client) {
        this.kubernetesClient = client;
    }

    @XmlElement
    public void setAgent(Agent builder) {
        this.builder = builder;
    }

    @XmlElement
    public void setConnect(Connect builder) {
        this.builder = builder;
    }

    @XmlElement
    public void setDisconnect(Disconnect builder) {
        this.builder = builder;
    }

    @XmlElement(name = "create-service")
    public void setCreateService(CreateService builder) {
        this.builder = builder;
    }

    @XmlElement(name = "create-secret")
    public void setCreateSecret(CreateSecret builder) {
        this.builder = builder;
    }

    @XmlElement(name = "create-config-map")
    public void setCreateConfigMap(CreateConfigMap builder) {
        this.builder = builder;
    }

    @XmlElement(name = "create-resource")
    public void setCreateResource(CreateResource builder) {
        this.builder = builder;
    }

    @XmlElement(name = "create-custom-resource")
    public void setCreateCustomResource(CreateCustomResource builder) {
        this.builder = builder;
    }

    @XmlElement(name = "create-labels")
    public void setCreateLabels(CreateLabels builder) {
        this.builder = builder;
    }

    @XmlElement(name = "create-annotations")
    public void setCreateAnnotations(CreateAnnotations builder) {
        this.builder = builder;
    }

    @XmlElement(name = "delete-service")
    public void setDeleteService(DeleteService builder) {
        this.builder = builder;
    }

    @XmlElement(name = "delete-secret")
    public void setDeleteSecret(DeleteSecret builder) {
        this.builder = builder;
    }

    @XmlElement(name = "delete-config-map")
    public void setDeleteConfigMap(DeleteConfigMap builder) {
        this.builder = builder;
    }

    @XmlElement(name = "delete-resource")
    public void setDeleteResource(DeleteResource builder) {
        this.builder = builder;
    }

    @XmlElement(name = "delete-custom-resource")
    public void setDeleteCustomResource(DeleteCustomResource builder) {
        this.builder = builder;
    }

    @XmlElement(name = "verify-pod")
    public void setVerifyPod(VerifyPod builder) {
        this.builder = builder;
    }

    @XmlElement(name = "verify-custom-resource")
    public void setVerifyCustomResource(VerifyCustomResource builder) {
        this.builder = builder;
    }

    @XmlElement(name = "watch-pod-logs")
    public void setWatchPodLogs(WatchPodLogs builder) {
        this.builder = builder;
    }

    @Override
    public KubernetesAction build() {
        if (builder == null) {
            throw new CitrusRuntimeException("Missing Kubernetes action - please provide proper action details");
        }

        if (builder instanceof TestActionContainerBuilder<?,?>) {
            ((TestActionContainerBuilder<?,?>) builder).getActions().stream()
                    .filter(action -> action instanceof ReferenceResolverAware)
                    .forEach(action -> ((ReferenceResolverAware) action).setReferenceResolver(referenceResolver));
        }

        if (builder instanceof ReferenceResolverAware) {
            ((ReferenceResolverAware) builder).setReferenceResolver(referenceResolver);
        }

        builder.description(description);
        builder.inNamespace(namespace);
        builder.autoRemoveResources(autoRemoveResources);

        if (referenceResolver != null) {
            if (kubernetesClient != null) {
                builder.client(referenceResolver.resolve(kubernetesClient, KubernetesClient.class));
            }

            if (actor != null) {
                builder.actor(referenceResolver.resolve(actor, TestActor.class));
            }
        }

        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
