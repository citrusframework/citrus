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

package org.citrusframework.docker.client;

import org.citrusframework.endpoint.AbstractEndpointBuilder;
import com.github.dockerjava.core.DefaultDockerClientConfig;

/**
 * @author Christoph Deppisch
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
     * @return
     */
    public DockerClientBuilder url(String host) {
        config.withDockerHost(host);
        return this;
    }

    /**
     * Sets the client version.
     * @param version
     * @return
     */
    public DockerClientBuilder version(String version) {
        config.withApiVersion(version);
        return this;
    }

    /**
     * Sets the client username.
     * @param username
     * @return
     */
    public DockerClientBuilder username(String username) {
        config.withRegistryUsername(username);
        return this;
    }

    /**
     * Sets the client password.
     * @param password
     * @return
     */
    public DockerClientBuilder password(String password) {
        config.withRegistryPassword(password);
        return this;
    }

    /**
     * Sets the client email.
     * @param email
     * @return
     */
    public DockerClientBuilder email(String email) {
        config.withRegistryEmail(email);
        return this;
    }

    /**
     * Sets the docker registry url.
     * @param url
     * @return
     */
    public DockerClientBuilder registry(String url) {
        config.withRegistryUrl(url);
        return this;
    }

    /**
     * Sets the TLS verification.
     * @param verify
     * @return
     */
    public DockerClientBuilder verifyTls(boolean verify) {
        config.withDockerTlsVerify(verify);
        return this;
    }

    /**
     * Sets the client certPath.
     * @param certPath
     * @return
     */
    public DockerClientBuilder certPath(String certPath) {
        config.withDockerCertPath(certPath);
        return this;
    }

    /**
     * Sets the client configPath.
     * @param configPath
     * @return
     */
    public DockerClientBuilder configPath(String configPath) {
        config.withDockerConfig(configPath);
        return this;
    }
}
