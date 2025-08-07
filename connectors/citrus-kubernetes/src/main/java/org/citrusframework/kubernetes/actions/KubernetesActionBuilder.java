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

import io.fabric8.kubernetes.client.KubernetesClient;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.kubernetes.KubernetesAgentActionBuilder;
import org.citrusframework.actions.kubernetes.KubernetesConfigMapActionBuilder;
import org.citrusframework.actions.kubernetes.KubernetesCustomResourceActionBuilder;
import org.citrusframework.actions.kubernetes.KubernetesDeploymentActionBuilder;
import org.citrusframework.actions.kubernetes.KubernetesPodActionBuilder;
import org.citrusframework.actions.kubernetes.KubernetesResourceActionBuilder;
import org.citrusframework.actions.kubernetes.KubernetesSecretActionBuilder;
import org.citrusframework.actions.kubernetes.KubernetesServiceActionBuilder;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kubernetes.endpoint.KubernetesEndpointConfiguration;
import org.springframework.util.Assert;

public class KubernetesActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<KubernetesAction>,
        org.citrusframework.actions.kubernetes.KubernetesActionBuilder<KubernetesAction, KubernetesActionBuilder> {

    /** Kubernetes client */
    private KubernetesClient kubernetesClient;

    private AbstractKubernetesAction.Builder<? extends KubernetesAction, ?> delegate;

    /**
     * Fluent API action building entry method used in Java DSL.
     */
    public static KubernetesActionBuilder k8s() {
        return kubernetes();
    }

    /**
     * Fluent API action building entry method used in Java DSL.
     */
    public static KubernetesActionBuilder kubernetes() {
        return new KubernetesActionBuilder();
    }

    /**
     * Use a custom Kubernetes client.
     * @param kubernetesClient
     */
    public KubernetesActionBuilder client(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
        return this;
    }

    @Override
    public KubernetesActionBuilder client(Object client) {
        if (client instanceof KubernetesClient k8sClient) {
            return client(k8sClient);
        } else if (client instanceof org.citrusframework.kubernetes.client.KubernetesClient k8sClient) {
            return client(k8sClient.getClient());
        } else {
            throw new CitrusRuntimeException("Kubernetes client must be of type %s".formatted(KubernetesClient.class.getName()));
        }
    }

    @Override
    public AgentActionBuilder agent() {
        return new AgentActionBuilder();
    }

    @Override
    public ServiceActionBuilder services() {
        return new ServiceActionBuilder();
    }

    @Override
    public ResourceActionBuilder resources() {
        return new ResourceActionBuilder();
    }

    @Override
    public DeploymentActionBuilder deployments() {
        return new DeploymentActionBuilder();
    }

    @Override
    public PodActionBuilder pods() {
        return new PodActionBuilder();
    }

    @Override
    public CustomResourceActionBuilder customResources() {
        return new CustomResourceActionBuilder();
    }

    @Override
    public SecretActionBuilder secrets() {
        return new SecretActionBuilder();
    }

    @Override
    public ConfigMapActionBuilder configMaps() {
        return new ConfigMapActionBuilder();
    }

    @Override
    public KubernetesExecuteAction.Builder execute() {
        KubernetesEndpointConfiguration endpointConfiguration = new KubernetesEndpointConfiguration();
        endpointConfiguration.setKubernetesClient(kubernetesClient);

        return new KubernetesExecuteAction.Builder()
                .client(new org.citrusframework.kubernetes.client.KubernetesClient(endpointConfiguration));

    }
    @Override
    public KubernetesAction build() {
        Assert.notNull(delegate, "Missing delegate action to build");
        if (kubernetesClient != null) {
            delegate.client(kubernetesClient);
        }
        return delegate.build();
    }

    @Override
    public TestActionBuilder<?> getDelegate() {
        return delegate;
    }

    public class SecretActionBuilder implements KubernetesSecretActionBuilder {

        @Override
        public CreateSecretAction.Builder create(String secretName) {
            CreateSecretAction.Builder builder = new CreateSecretAction.Builder()
                    .client(kubernetesClient)
                    .secret(secretName);
            delegate = builder;
            return builder;
        }

        @Override
        public CreateAnnotationsAction.Builder addAnnotation(String secretName) {
            CreateAnnotationsAction.Builder builder = new CreateAnnotationsAction.Builder()
                    .client(kubernetesClient)
                    .secret(secretName);
            delegate = builder;
            return builder;
        }

        @Override
        public CreateLabelsAction.Builder addLabel(String secretName) {
            CreateLabelsAction.Builder builder = new CreateLabelsAction.Builder()
                    .client(kubernetesClient)
                    .secret(secretName);
            delegate = builder;
            return builder;
        }

        @Override
        public DeleteSecretAction.Builder delete(String secretName) {
            DeleteSecretAction.Builder builder = new DeleteSecretAction.Builder()
                    .client(kubernetesClient)
                    .secret(secretName);
            delegate = builder;
            return builder;
        }
    }

    public class ConfigMapActionBuilder implements KubernetesConfigMapActionBuilder {

        @Override
        public CreateConfigMapAction.Builder create(String configMapName) {
            CreateConfigMapAction.Builder builder = new CreateConfigMapAction.Builder()
                    .client(kubernetesClient)
                    .configMap(configMapName);
            delegate = builder;
            return builder;
        }

        @Override
        public CreateAnnotationsAction.Builder addAnnotation(String configMapName) {
            CreateAnnotationsAction.Builder builder = new CreateAnnotationsAction.Builder()
                    .client(kubernetesClient)
                    .configMap(configMapName);
            delegate = builder;
            return builder;
        }

        @Override
        public CreateLabelsAction.Builder addLabel(String configMapName) {
            CreateLabelsAction.Builder builder = new CreateLabelsAction.Builder()
                    .client(kubernetesClient)
                    .configMap(configMapName);
            delegate = builder;
            return builder;
        }

        @Override
        public DeleteConfigMapAction.Builder delete(String configMapName) {
            DeleteConfigMapAction.Builder builder = new DeleteConfigMapAction.Builder()
                    .client(kubernetesClient)
                    .configMap(configMapName);
            delegate = builder;
            return builder;
        }
    }

    public class CustomResourceActionBuilder implements KubernetesCustomResourceActionBuilder {

        @Override
        public CreateCustomResourceAction.Builder create() {
            CreateCustomResourceAction.Builder builder = new CreateCustomResourceAction.Builder()
                    .client(kubernetesClient);
            delegate = builder;
            return builder;
        }

        @Override
        public DeleteCustomResourceAction.Builder delete(String name) {
            DeleteCustomResourceAction.Builder builder = new DeleteCustomResourceAction.Builder()
                    .client(kubernetesClient)
                    .resourceName(name);
            delegate = builder;
            return builder;
        }

        @Override
        public VerifyCustomResourceAction.Builder verify() {
            VerifyCustomResourceAction.Builder builder = new VerifyCustomResourceAction.Builder()
                    .client(kubernetesClient);
            delegate = builder;
            return builder;
        }
    }

    public class DeploymentActionBuilder implements KubernetesDeploymentActionBuilder {

        @Override
        public CreateAnnotationsAction.Builder addAnnotation(String deploymentName) {
            CreateAnnotationsAction.Builder builder = new CreateAnnotationsAction.Builder()
                    .client(kubernetesClient)
                    .deployment(deploymentName);
            delegate = builder;
            return builder;
        }

        @Override
        public CreateLabelsAction.Builder addLabel(String deploymentName) {
            CreateLabelsAction.Builder builder = new CreateLabelsAction.Builder()
                    .client(kubernetesClient)
                    .deployment(deploymentName);
            delegate = builder;
            return builder;
        }

    }

    public class PodActionBuilder implements KubernetesPodActionBuilder {

        @Override
        public DeletePodAction.Builder delete() {
            DeletePodAction.Builder builder = new DeletePodAction.Builder()
                    .client(kubernetesClient);
            delegate = builder;
            return builder;
        }

        @Override
        public DeletePodAction.Builder delete(String podName) {
            DeletePodAction.Builder builder = new DeletePodAction.Builder()
                    .client(kubernetesClient)
                    .podName(podName);
            delegate = builder;
            return builder;
        }

        @Override
        public DeletePodAction.Builder delete(String label, String value) {
            DeletePodAction.Builder builder = new DeletePodAction.Builder()
                    .client(kubernetesClient)
                    .label(label, value);
            delegate = builder;
            return builder;
        }

        @Override
        public VerifyPodAction.Builder verify(String podName) {
            VerifyPodAction.Builder builder = new VerifyPodAction.Builder()
                    .client(kubernetesClient)
                    .podName(podName);
            delegate = builder;
            return builder;
        }

        @Override
        public WatchPodLogsAction.Builder watchLogs(String podName) {
            WatchPodLogsAction.Builder builder = new WatchPodLogsAction.Builder()
                    .client(kubernetesClient)
                    .podName(podName);
            delegate = builder;
            return builder;
        }

        @Override
        public WatchPodLogsAction.Builder watchLogs(String label, String value) {
            WatchPodLogsAction.Builder builder = new WatchPodLogsAction.Builder()
                    .client(kubernetesClient)
                    .label(label, value);
            delegate = builder;
            return builder;
        }

        @Override
        public CreateAnnotationsAction.Builder addAnnotation(String podName) {
            CreateAnnotationsAction.Builder builder = new CreateAnnotationsAction.Builder()
                    .client(kubernetesClient)
                    .pod(podName);
            delegate = builder;
            return builder;
        }

        @Override
        public CreateLabelsAction.Builder addLabel(String podName) {
            CreateLabelsAction.Builder builder = new CreateLabelsAction.Builder()
                    .client(kubernetesClient)
                    .pod(podName);
            delegate = builder;
            return builder;
        }

        @Override
        public VerifyPodAction.Builder verify(String label, String value) {
            VerifyPodAction.Builder builder = new VerifyPodAction.Builder()
                    .client(kubernetesClient)
                    .label(label, value);
            delegate = builder;
            return builder;
        }
    }

    public class ResourceActionBuilder implements KubernetesResourceActionBuilder {

        @Override
        public CreateResourceAction.Builder create() {
            CreateResourceAction.Builder builder = new CreateResourceAction.Builder()
                    .client(kubernetesClient);
            delegate = builder;
            return builder;
        }

        @Override
        public CreateAnnotationsAction.Builder addAnnotation(String resourceName, String resourceType) {
            CreateAnnotationsAction.Builder builder = new CreateAnnotationsAction.Builder()
                    .client(kubernetesClient)
                    .resource(resourceName)
                    .type(resourceType);
            delegate = builder;
            return builder;
        }

        @Override
        public CreateLabelsAction.Builder addLabel(String resourceName, String resourceType) {
            CreateLabelsAction.Builder builder = new CreateLabelsAction.Builder()
                    .client(kubernetesClient)
                    .resource(resourceName)
                    .type(resourceType);
            delegate = builder;
            return builder;
        }

        @Override
        public DeleteResourceAction.Builder delete(String content) {
            DeleteResourceAction.Builder builder = new DeleteResourceAction.Builder()
                    .client(kubernetesClient)
                    .content(content);
            delegate = builder;
            return builder;
        }
    }

    public class AgentActionBuilder implements KubernetesAgentActionBuilder {

        @Override
        public AgentConnectAction.Builder connect() {
            AgentConnectAction.Builder builder = new AgentConnectAction.Builder()
                    .client(kubernetesClient);
            delegate = builder;
            return builder;
        }

        @Override
        public AgentConnectAction.Builder connect(String agentName) {
            AgentConnectAction.Builder builder = new AgentConnectAction.Builder()
                    .client(kubernetesClient)
                    .agent(agentName);
            delegate = builder;
            return builder;
        }

        @Override
        public AgentDisconnectAction.Builder disconnect() {
            AgentDisconnectAction.Builder builder = new AgentDisconnectAction.Builder()
                    .client(kubernetesClient);
            delegate = builder;
            return builder;
        }

        @Override
        public AgentDisconnectAction.Builder disconnect(String agentName) {
            AgentDisconnectAction.Builder builder = new AgentDisconnectAction.Builder()
                    .client(kubernetesClient)
                    .agent(agentName);
            delegate = builder;
            return builder;
        }
    }

    public class ServiceActionBuilder implements KubernetesServiceActionBuilder {

        @Override
        public ServiceConnectAction.Builder connect() {
            ServiceConnectAction.Builder builder = new ServiceConnectAction.Builder()
                    .client(kubernetesClient);
            delegate = builder;
            return builder;
        }

        @Override
        public ServiceConnectAction.Builder connect(String serviceName) {
            ServiceConnectAction.Builder builder = new ServiceConnectAction.Builder()
                    .client(kubernetesClient)
                    .service(serviceName);
            delegate = builder;
            return builder;
        }

        @Override
        public ServiceDisconnectAction.Builder disconnect() {
            ServiceDisconnectAction.Builder builder = new ServiceDisconnectAction.Builder()
                    .client(kubernetesClient);
            delegate = builder;
            return builder;
        }

        @Override
        public ServiceDisconnectAction.Builder disconnect(String serviceName) {
            ServiceDisconnectAction.Builder builder = new ServiceDisconnectAction.Builder()
                    .client(kubernetesClient)
                    .service(serviceName);
            delegate = builder;
            return builder;
        }

        @Override
        public CreateServiceAction.Builder create(String serviceName) {
            CreateServiceAction.Builder builder = new CreateServiceAction.Builder()
                    .client(kubernetesClient)
                    .service(serviceName);
            delegate = builder;
            return builder;
        }

        @Override
        public CreateAnnotationsAction.Builder addAnnotation(String serviceName) {
            CreateAnnotationsAction.Builder builder = new CreateAnnotationsAction.Builder()
                    .client(kubernetesClient)
                    .service(serviceName);
            delegate = builder;
            return builder;
        }

        @Override
        public CreateLabelsAction.Builder addLabel(String serviceName) {
            CreateLabelsAction.Builder builder = new CreateLabelsAction.Builder()
                    .client(kubernetesClient)
                    .service(serviceName);
            delegate = builder;
            return builder;
        }

        @Override
        public DeleteServiceAction.Builder delete() {
            DeleteServiceAction.Builder builder = new DeleteServiceAction.Builder()
                    .client(kubernetesClient);
            delegate = builder;
            return builder;
        }

        @Override
        public DeleteServiceAction.Builder delete(String serviceName) {
            DeleteServiceAction.Builder builder = new DeleteServiceAction.Builder()
                    .client(kubernetesClient)
                    .service(serviceName);
            delegate = builder;
            return builder;
        }
    }
}
