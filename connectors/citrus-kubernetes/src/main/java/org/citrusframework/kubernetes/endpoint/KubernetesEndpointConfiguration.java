/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.kubernetes.endpoint;

import org.citrusframework.endpoint.AbstractPollableEndpointConfiguration;
import org.citrusframework.kubernetes.message.KubernetesMessageConverter;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.MessageCorrelator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.client.*;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class KubernetesEndpointConfiguration extends AbstractPollableEndpointConfiguration {

    /** Kubernetes client configuration */
    private Config kubernetesClientConfig;

    /** Java kubernetes client */
    private io.fabric8.kubernetes.client.KubernetesClient kubernetesClient;

    /** Reply message correlator */
    private MessageCorrelator correlator = new DefaultMessageCorrelator();

    /** JSON data binding for command result */
    private ObjectMapper objectMapper = new ObjectMapper();

    /** Kubernetes message converter */
    private KubernetesMessageConverter messageConverter = new KubernetesMessageConverter();

    /**
     * Creates new Kubernetes client instance with configuration.
     * @return
     */
    private io.fabric8.kubernetes.client.KubernetesClient createKubernetesClient() {
        return new DefaultKubernetesClient(getKubernetesClientConfig());
    }

    /**
     * Constructs or gets the kubernetes client implementation.
     * @return
     */
    public io.fabric8.kubernetes.client.KubernetesClient getKubernetesClient() {
        if (kubernetesClient == null) {
            kubernetesClient = createKubernetesClient();
        }

        return kubernetesClient;
    }

    /**
     * Sets the kubernetesClient property.
     *
     * @param kubernetesClient
     */
    public void setKubernetesClient(io.fabric8.kubernetes.client.KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    /**
     * Gets the kubernetes client configuration.
     * @return
     */
    public Config getKubernetesClientConfig() {
        if (kubernetesClientConfig == null) {
            kubernetesClientConfig = new ConfigBuilder().build();
        }

        return kubernetesClientConfig;
    }

    /**
     * Sets the kubernetes client configuration.
     * @param kubernetesClientConfig
     */
    public void setKubernetesClientConfig(Config kubernetesClientConfig) {
        this.kubernetesClientConfig = kubernetesClientConfig;
    }

    /**
     * Set the reply message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(MessageCorrelator correlator) {
        this.correlator = correlator;
    }

    /**
     * Gets the correlator.
     * @return the correlator
     */
    public MessageCorrelator getCorrelator() {
        return correlator;
    }

    /**
     * Gets the kubernetes message converter.
     * @return
     */
    public KubernetesMessageConverter getMessageConverter() {
        return messageConverter;
    }

    /**
     * Sets the kubernetes message converter.
     * @param messageConverter
     */
    public void setMessageConverter(KubernetesMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Gets the command object mapper.
     * @return
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Sets the command object mapper.
     * @param objectMapper
     */
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
