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

package org.citrusframework.docker.client;

import org.citrusframework.endpoint.AbstractEndpointBuilder;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import org.citrusframework.yaml.SchemaProperty;

/**
 * @since 2.5
 */
public class DockerClientBuilder extends AbstractEndpointBuilder<DockerClient> {

    /** Endpoint target */
    private final DockerClient endpoint = new DockerClient();
    private final DefaultDockerClientConfig.Builder config = DefaultDockerClientConfig.createDefaultConfigBuilder();

    @Override
    public DockerClient build() {
        endpoint.getEndpointConfiguration().setDockerClientConfig(config.build());
        return super.build();
    }

    @Override
    protected DockerClient getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the docker host url.
     */
    public DockerClientBuilder url(String host) {
        config.withDockerHost(host);
        return this;
    }

    @SchemaProperty(description = "The Docker host engine URL.")
    public void setUrl(String url) {
        url(url);
    }

    /**
     * Sets the client version.
     */
    public DockerClientBuilder version(String version) {
        config.withApiVersion(version);
        return this;
    }

    @SchemaProperty(description = "The Docker client version.")
    public void setVersion(String version) {
        version(version);
    }

    /**
     * Sets the client username.
     */
    public DockerClientBuilder username(String username) {
        config.withRegistryUsername(username);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "The Docker client username.")
    public void setUsername(String username) {
        username(username);
    }

    /**
     * Sets the client password.
     */
    public DockerClientBuilder password(String password) {
        config.withRegistryPassword(password);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "The Docker client password.")
    public void setPassword(String password) {
        password(password);
    }

    /**
     * Sets the client email.
     */
    public DockerClientBuilder email(String email) {
        config.withRegistryEmail(email);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "The Docker client email."
    )
    public void setEmail(String email) {
        email(email);
    }

    /**
     * Sets the docker registry url.
     */
    public DockerClientBuilder registry(String url) {
        config.withRegistryUrl(url);
        return this;
    }

    @SchemaProperty(description = "The Docker registry URL.")
    public void setRegistry(String url) {
        url(url);
    }

    /**
     * Sets the TLS verification.
     */
    public DockerClientBuilder verifyTls(boolean verify) {
        config.withDockerTlsVerify(verify);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "When enabled the client verifies the Docker host TLS.")
    public void setVerifyTls(boolean verify) {
        verifyTls(verify);
    }

    /**
     * Sets the client certPath.
     */
    public DockerClientBuilder certPath(String certPath) {
        config.withDockerCertPath(certPath);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Sets the path to the client certificate.")
    public void setCertPath(String certPath) {
        certPath(certPath);
    }
    /**
     * Sets the client configPath.
     */
    public DockerClientBuilder configPath(String configPath) {
        config.withDockerConfig(configPath);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the path to the client configuration file.")
    public void setConfigPath(String configPath) {
        configPath(configPath);
    }
}
