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

import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.citrusframework.TestActionBuilder;
import org.springframework.util.Assert;

public class KubernetesActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<KubernetesAction> {

    /** Kubernetes client */
    private KubernetesClient kubernetesClient;

    private AbstractKubernetesAction.Builder<? extends KubernetesAction, ?> delegate;

    /**
     * Fluent API action building entry method used in Java DSL.
     * @return
     */
    public static KubernetesActionBuilder k8s() {
        return kubernetes();
    }

    /**
     * Fluent API action building entry method used in Java DSL.
     * @return
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

    /**
     * Performs actions on Kubernetes agent.
     * @return
     */
    public AgentActionBuilder agent() {
        return new AgentActionBuilder();
    }

    /**
     * Performs actions on Kubernetes services.
     * @return
     */
    public ServiceActionBuilder services() {
        return new ServiceActionBuilder();
    }

    /**
     * Performs actions on Kubernetes resources.
     * @return
     */
    public ResourceActionBuilder resources() {
        return new ResourceActionBuilder();
    }

    /**
     * Performs actions on Kubernetes pods.
     * @return
     */
    public DeploymentActionBuilder deployments() {
        return new DeploymentActionBuilder();
    }

    /**
     * Performs actions on Kubernetes pods.
     * @return
     */
    public PodActionBuilder pods() {
        return new PodActionBuilder();
    }

    /**
     * Performs actions on Kubernetes custom resources.
     * @return
     */
    public CustomResourceActionBuilder customResources() {
        return new CustomResourceActionBuilder();
    }

    /**
     * Performs actions on Kubernetes secrets.
     * @return
     */
    public SecretActionBuilder secrets() {
        return new SecretActionBuilder();
    }

    /**
     * Performs actions on Kubernetes config maps.
     * @return
     */
    public ConfigMapActionBuilder configMaps() {
        return new ConfigMapActionBuilder();
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

    public class SecretActionBuilder {
        /**
         * Create secret instance.
         * @param secretName the name of the Kubernetes secret.
         */
        public CreateSecretAction.Builder create(String secretName) {
            CreateSecretAction.Builder builder = new CreateSecretAction.Builder()
                    .client(kubernetesClient)
                    .secret(secretName);
            delegate = builder;
            return builder;
        }

        /**
         * Add annotation on secret instance.
         * @param secretName the name of the Kubernetes secret.
         */
        public CreateAnnotationsAction.Builder addAnnotation(String secretName) {
            CreateAnnotationsAction.Builder builder = new CreateAnnotationsAction.Builder()
                    .client(kubernetesClient)
                    .secret(secretName);
            delegate = builder;
            return builder;
        }

        /**
         * Add label on secret instance.
         * @param secretName the name of the Kubernetes secret.
         */
        public CreateLabelsAction.Builder addLabel(String secretName) {
            CreateLabelsAction.Builder builder = new CreateLabelsAction.Builder()
                    .client(kubernetesClient)
                    .secret(secretName);
            delegate = builder;
            return builder;
        }

        /**
         * Delete secret instance.
         * @param secretName the name of the Kubernetes secret.
         */
        public DeleteSecretAction.Builder delete(String secretName) {
            DeleteSecretAction.Builder builder = new DeleteSecretAction.Builder()
                    .client(kubernetesClient)
                    .secret(secretName);
            delegate = builder;
            return builder;
        }
    }

    public class ConfigMapActionBuilder {
        /**
         * Create configMap instance.
         * @param configMapName the name of the Kubernetes configMap.
         */
        public CreateConfigMapAction.Builder create(String configMapName) {
            CreateConfigMapAction.Builder builder = new CreateConfigMapAction.Builder()
                    .client(kubernetesClient)
                    .configMap(configMapName);
            delegate = builder;
            return builder;
        }

        /**
         * Add annotation on configMap instance.
         * @param configMapName the name of the Kubernetes configMap.
         */
        public CreateAnnotationsAction.Builder addAnnotation(String configMapName) {
            CreateAnnotationsAction.Builder builder = new CreateAnnotationsAction.Builder()
                    .client(kubernetesClient)
                    .configMap(configMapName);
            delegate = builder;
            return builder;
        }

        /**
         * Add label on configMap instance.
         * @param configMapName the name of the Kubernetes configMap.
         */
        public CreateLabelsAction.Builder addLabel(String configMapName) {
            CreateLabelsAction.Builder builder = new CreateLabelsAction.Builder()
                    .client(kubernetesClient)
                    .configMap(configMapName);
            delegate = builder;
            return builder;
        }

        /**
         * Delete configMap instance.
         * @param configMapName the name of the Kubernetes configMap.
         */
        public DeleteConfigMapAction.Builder delete(String configMapName) {
            DeleteConfigMapAction.Builder builder = new DeleteConfigMapAction.Builder()
                    .client(kubernetesClient)
                    .configMap(configMapName);
            delegate = builder;
            return builder;
        }
    }

    public class CustomResourceActionBuilder {
        /**
         * Create custom resource instance.
         */
        public CreateCustomResourceAction.Builder create() {
            CreateCustomResourceAction.Builder builder = new CreateCustomResourceAction.Builder()
                    .client(kubernetesClient);
            delegate = builder;
            return builder;
        }

        /**
         * Delete custom resource instance.
         * @param name the name of the Kubernetes custom resource.
         */
        public DeleteCustomResourceAction.Builder delete(String name) {
            DeleteCustomResourceAction.Builder builder = new DeleteCustomResourceAction.Builder()
                    .client(kubernetesClient)
                    .resourceName(name);
            delegate = builder;
            return builder;
        }

        /**
         * Verify that given custom resource matches a condition.
         */
        public VerifyCustomResourceAction.Builder verify() {
            VerifyCustomResourceAction.Builder builder = new VerifyCustomResourceAction.Builder()
                    .client(kubernetesClient);
            delegate = builder;
            return builder;
        }

        /**
         * Verify that given custom resource matches a condition.
         * @param resourceType the type of the customer resource.
         */
        public VerifyCustomResourceAction.Builder verify(Class<? extends CustomResource<?, ?>> resourceType) {
            VerifyCustomResourceAction.Builder builder = new VerifyCustomResourceAction.Builder()
                    .client(kubernetesClient)
                    .type(resourceType);
            delegate = builder;
            return builder;
        }

        /**
         * Verify that given custom resource matches a condition.
         * @param name the name of the custom resource.
         * @param resourceType the type of the customer resource.
         */
        public VerifyCustomResourceAction.Builder verify(String name, Class<? extends CustomResource<?, ?>> resourceType) {
            VerifyCustomResourceAction.Builder builder = new VerifyCustomResourceAction.Builder()
                    .client(kubernetesClient)
                    .type(resourceType)
                    .resourceName(name);
            delegate = builder;
            return builder;
        }

        /**
         * Verify that given custom resource matches a condition.
         * @param name the name of the custom resource.
         */
        public VerifyCustomResourceAction.Builder verify(String name) {
            VerifyCustomResourceAction.Builder builder = new VerifyCustomResourceAction.Builder()
                    .client(kubernetesClient)
                    .resourceName(name);
            delegate = builder;
            return builder;
        }

        /**
         * Verify that given custom resource matches a condition.
         * @param label the label to filter results.
         * @param value the value of the label.
         */
        public VerifyCustomResourceAction.Builder verify(String label, String value) {
            VerifyCustomResourceAction.Builder builder = new VerifyCustomResourceAction.Builder()
                    .client(kubernetesClient)
                    .label(label, value);
            delegate = builder;
            return builder;
        }
    }

    public class DeploymentActionBuilder {
        /**
         * Add annotation on deployment instance.
         * @param deploymentName the name of the Kubernetes deployment.
         */
        public CreateAnnotationsAction.Builder addAnnotation(String deploymentName) {
            CreateAnnotationsAction.Builder builder = new CreateAnnotationsAction.Builder()
                    .client(kubernetesClient)
                    .deployment(deploymentName);
            delegate = builder;
            return builder;
        }

        /**
         * Add label on deployment instance.
         * @param deploymentName the name of the Kubernetes deployment.
         */
        public CreateLabelsAction.Builder addLabel(String deploymentName) {
            CreateLabelsAction.Builder builder = new CreateLabelsAction.Builder()
                    .client(kubernetesClient)
                    .deployment(deploymentName);
            delegate = builder;
            return builder;
        }

    }

    public class PodActionBuilder {
        /**
         * Verify that given pod is running.
         * @param podName the name of the Camel K pod.
         */
        public VerifyPodAction.Builder verify(String podName) {
            VerifyPodAction.Builder builder = new VerifyPodAction.Builder()
                    .client(kubernetesClient)
                    .podName(podName);
            delegate = builder;
            return builder;
        }

        /**
         * Watch pod logs for given pod identified by its name.
         * @param podName the name of the Camel K pod.
         */
        public WatchPodLogsAction.Builder watchLogs(String podName) {
            WatchPodLogsAction.Builder builder = new WatchPodLogsAction.Builder()
                    .client(kubernetesClient)
                    .podName(podName);
            delegate = builder;
            return builder;
        }

        /**
         * Watch pod logs for given pod identified by label selector.
         * @param label the name of the pod label to filter on.
         * @param value the value of the pod label to match.
         */
        public WatchPodLogsAction.Builder watchLogs(String label, String value) {
            WatchPodLogsAction.Builder builder = new WatchPodLogsAction.Builder()
                    .client(kubernetesClient)
                    .label(label, value);
            delegate = builder;
            return builder;
        }

        /**
         * Add annotation on pod instance.
         * @param podName the name of the Kubernetes pod.
         */
        public CreateAnnotationsAction.Builder addAnnotation(String podName) {
            CreateAnnotationsAction.Builder builder = new CreateAnnotationsAction.Builder()
                    .client(kubernetesClient)
                    .pod(podName);
            delegate = builder;
            return builder;
        }

        /**
         * Add label on pod instance.
         * @param podName the name of the Kubernetes pod.
         */
        public CreateLabelsAction.Builder addLabel(String podName) {
            CreateLabelsAction.Builder builder = new CreateLabelsAction.Builder()
                    .client(kubernetesClient)
                    .pod(podName);
            delegate = builder;
            return builder;
        }

        /**
         * Verify that given pod is running.
         * @param label the name of the pod label to filter on.
         * @param value the value of the pod label to match.
         */
        public VerifyPodAction.Builder verify(String label, String value) {
            VerifyPodAction.Builder builder = new VerifyPodAction.Builder()
                    .client(kubernetesClient)
                    .label(label, value);
            delegate = builder;
            return builder;
        }
    }

    public class ResourceActionBuilder {
        /**
         * Create any Kubernetes resource instance from yaml.
         */
        public CreateResourceAction.Builder create() {
            CreateResourceAction.Builder builder = new CreateResourceAction.Builder()
                    .client(kubernetesClient);
            delegate = builder;
            return builder;
        }

        /**
         * Add annotation on resource instance.
         * @param resourceName the name of the Kubernetes resource.
         * @param resourceType the type of the Kubernetes resource.
         */
        public CreateAnnotationsAction.Builder addAnnotation(String resourceName, String resourceType) {
            CreateAnnotationsAction.Builder builder = new CreateAnnotationsAction.Builder()
                    .client(kubernetesClient)
                    .resource(resourceName)
                    .type(resourceType);
            delegate = builder;
            return builder;
        }

        /**
         * Add label on resource instance.
         * @param resourceName the name of the Kubernetes resource.
         * @param resourceType the type of the Kubernetes resource.
         */
        public CreateLabelsAction.Builder addLabel(String resourceName, String resourceType) {
            CreateLabelsAction.Builder builder = new CreateLabelsAction.Builder()
                    .client(kubernetesClient)
                    .resource(resourceName)
                    .type(resourceType);
            delegate = builder;
            return builder;
        }

        /**
         * Delete any Kubernetes resource instance.
         * @param content the Kubernetes resource as YAML content.
         */
        public DeleteResourceAction.Builder delete(String content) {
            DeleteResourceAction.Builder builder = new DeleteResourceAction.Builder()
                    .client(kubernetesClient)
                    .content(content);
            delegate = builder;
            return builder;
        }
    }

    public class AgentActionBuilder {

        /**
         * Create new agent deployment and connect to given Kubernetes service via local port forward.
         */
        public AgentConnectAction.Builder connect() {
            AgentConnectAction.Builder builder = new AgentConnectAction.Builder()
                    .client(kubernetesClient);
            delegate = builder;
            return builder;
        }
        /**
         * Create new agent deployment and connect to given Kubernetes service via local port forward.
         */
        public AgentConnectAction.Builder connect(String agentName) {
            AgentConnectAction.Builder builder = new AgentConnectAction.Builder()
                    .client(kubernetesClient)
                    .agent(agentName);
            delegate = builder;
            return builder;
        }

        /**
         * Disconnect from given Kubernetes agent.
         */
        public AgentDisconnectAction.Builder disconnect() {
            AgentDisconnectAction.Builder builder = new AgentDisconnectAction.Builder()
                    .client(kubernetesClient);
            delegate = builder;
            return builder;
        }

        /**
         * Disconnect from given Kubernetes agent.
         */
        public AgentDisconnectAction.Builder disconnect(String agentName) {
            AgentDisconnectAction.Builder builder = new AgentDisconnectAction.Builder()
                    .client(kubernetesClient)
                    .agent(agentName);
            delegate = builder;
            return builder;
        }
    }

    public class ServiceActionBuilder {

        /**
         * Connect to given Kubernetes service via local port forward.
         */
        public ServiceConnectAction.Builder connect() {
            ServiceConnectAction.Builder builder = new ServiceConnectAction.Builder()
                    .client(kubernetesClient);
            delegate = builder;
            return builder;
        }

        /**
         * Connect to given Kubernetes service via local port forward.
         * @param serviceName the name of the Kubernetes service.
         */
        public ServiceConnectAction.Builder connect(String serviceName) {
            ServiceConnectAction.Builder builder = new ServiceConnectAction.Builder()
                    .client(kubernetesClient)
                    .service(serviceName);
            delegate = builder;
            return builder;
        }

        /**
         * Connect to given Kubernetes service via local port forward.
         */
        public ServiceDisconnectAction.Builder disconnect() {
            ServiceDisconnectAction.Builder builder = new ServiceDisconnectAction.Builder()
                    .client(kubernetesClient);
            delegate = builder;
            return builder;
        }

        /**
         * Connect to given Kubernetes service via local port forward.
         * @param serviceName the name of the Kubernetes service.
         */
        public ServiceDisconnectAction.Builder disconnect(String serviceName) {
            ServiceDisconnectAction.Builder builder = new ServiceDisconnectAction.Builder()
                    .client(kubernetesClient)
                    .service(serviceName);
            delegate = builder;
            return builder;
        }

        /**
         * Create service instance.
         * @param serviceName the name of the Kubernetes service.
         */
        public CreateServiceAction.Builder create(String serviceName) {
            CreateServiceAction.Builder builder = new CreateServiceAction.Builder()
                    .client(kubernetesClient)
                    .service(serviceName);
            delegate = builder;
            return builder;
        }

        /**
         * Add annotation on service instance.
         * @param serviceName the name of the Kubernetes service.
         */
        public CreateAnnotationsAction.Builder addAnnotation(String serviceName) {
            CreateAnnotationsAction.Builder builder = new CreateAnnotationsAction.Builder()
                    .client(kubernetesClient)
                    .service(serviceName);
            delegate = builder;
            return builder;
        }

        /**
         * Add label on service instance.
         * @param serviceName the name of the Kubernetes service.
         */
        public CreateLabelsAction.Builder addLabel(String serviceName) {
            CreateLabelsAction.Builder builder = new CreateLabelsAction.Builder()
                    .client(kubernetesClient)
                    .service(serviceName);
            delegate = builder;
            return builder;
        }

        /**
         * Delete service instance.
         * @param serviceName the name of the Kubernetes service.
         */
        public DeleteServiceAction.Builder delete(String serviceName) {
            DeleteServiceAction.Builder builder = new DeleteServiceAction.Builder()
                    .client(kubernetesClient)
                    .service(serviceName);
            delegate = builder;
            return builder;
        }
    }
}
