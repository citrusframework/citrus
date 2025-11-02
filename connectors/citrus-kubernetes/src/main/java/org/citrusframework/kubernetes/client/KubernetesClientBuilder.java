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

package org.citrusframework.kubernetes.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.client.ConfigBuilder;
import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.kubernetes.message.KubernetesMessageConverter;
import org.citrusframework.util.StringUtils;
import org.citrusframework.yaml.SchemaProperty;

/**
 * @since 2.7
 */
public class KubernetesClientBuilder extends AbstractEndpointBuilder<KubernetesClient> {

    /** Endpoint target */
    private final KubernetesClient endpoint = new KubernetesClient();
    private final ConfigBuilder config = new ConfigBuilder();

    private String client;
    private String messageConverter;
    private String objectMapper;

    @Override
    public KubernetesClient build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(client)) {
                client(referenceResolver.resolve(client, io.fabric8.kubernetes.client.KubernetesClient.class));
            }

            if (StringUtils.hasText(messageConverter)) {
                messageConverter(referenceResolver.resolve(messageConverter, KubernetesMessageConverter.class));
            }

            if (StringUtils.hasText(objectMapper)) {
                objectMapper(referenceResolver.resolve(objectMapper, ObjectMapper.class));
            }
        }

        endpoint.getEndpointConfiguration().setKubernetesClientConfig(config.build());
        return super.build();
    }

    @Override
    protected KubernetesClient getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the Fabric8 client instance.
     */
    public KubernetesClientBuilder client(io.fabric8.kubernetes.client.KubernetesClient client) {
        endpoint.getEndpointConfiguration().setKubernetesClient(client);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the reference to the Kubernetes client implementation.")
    public void setClient(String client) {
        this.client = client;
    }

    /**
     * Sets the client url.
     */
    public KubernetesClientBuilder url(String url) {
        config.withMasterUrl(url);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the master URL in the client configuration.")
    public void setUrl(String url) {
        url(url);
    }

    /**
     * Sets the client version.
     */
    public KubernetesClientBuilder version(String version) {
        config.withApiVersion(version);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the Kubernetes client version.")
    public void setVersion(String version) {
        version(version);
    }

    /**
     * Sets the client username.
     */
    public KubernetesClientBuilder username(String username) {
        config.withUsername(username);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Sets the user name used to connect to the Kubernetes cluster."
    )
    public void setUsername(String username) {
        username(username);
    }

    /**
     * Sets the client password.
     */
    public KubernetesClientBuilder password(String password) {
        config.withPassword(password);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Sets the user password used to connect to the Kubernetes cluster."
    )
    public void setPassword(String password) {
        password(password);
    }

    /**
     * Sets the authentication token.
     */
    public KubernetesClientBuilder oauthToken(String oauthToken) {
        config.withOauthToken(oauthToken);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Sets the OAuth token used to connect to the Kubernetes cluster."
    )
    public void setOAuthToken(String oauthToken) {
        oauthToken(oauthToken);
    }

    /**
     * Sets the client namespace.
     */
    public KubernetesClientBuilder namespace(String namespace) {
        config.withNamespace(namespace);
        return this;
    }

    @SchemaProperty(description = "Sets the namespace on the cluster.")
    public void setNamespace(String namespace) {
        namespace(namespace);
    }

    /**
     * Sets the client certFile.
     */
    public KubernetesClientBuilder certFile(String certFile) {
        config.withCaCertFile(certFile);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Sets the client certificate."
    )
    public void setCertFile(String certFile) {
        certFile(certFile);
    }

    /**
     * Sets the message converter.
     */
    public KubernetesClientBuilder messageConverter(KubernetesMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the message converter as a bean reference.")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Sets the object mapper.
     */
    public KubernetesClientBuilder objectMapper(ObjectMapper objectMapper) {
        endpoint.getEndpointConfiguration().setObjectMapper(objectMapper);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the object mapper.")
    public void setObjectMapper(String objectMapper) {
        this.objectMapper = objectMapper;
    }
}
