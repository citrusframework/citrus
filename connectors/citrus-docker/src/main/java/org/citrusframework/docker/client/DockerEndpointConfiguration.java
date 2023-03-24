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

import org.citrusframework.endpoint.AbstractPollableEndpointConfiguration;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.MessageCorrelator;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.okhttp.OkDockerHttpClient;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class DockerEndpointConfiguration extends AbstractPollableEndpointConfiguration {

    /** Docker client configuration */
    private DockerClientConfig dockerClientConfig;

    /** Java docker client */
    private com.github.dockerjava.api.DockerClient dockerClient;

    /** Reply message correlator */
    private MessageCorrelator correlator = new DefaultMessageCorrelator();

    /**
     * Creates new Docker client instance with configuration.
     * @return
     */
    private com.github.dockerjava.api.DockerClient createDockerClient() {
        return DockerClientImpl.getInstance(getDockerClientConfig(), new OkDockerHttpClient.Builder()
                        .dockerHost(getDockerClientConfig().getDockerHost())
                .build());
    }

    /**
     * Constructs or gets the docker client implementation.
     * @return
     */
    public com.github.dockerjava.api.DockerClient getDockerClient() {
        if (dockerClient == null) {
            dockerClient = createDockerClient();
        }

        return dockerClient;
    }

    /**
     * Sets the dockerClient property.
     *
     * @param dockerClient
     */
    public void setDockerClient(com.github.dockerjava.api.DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    /**
     * Gets the docker client configuration.
     * @return
     */
    public DockerClientConfig getDockerClientConfig() {
        if (dockerClientConfig == null) {
            dockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        }

        return dockerClientConfig;
    }

    /**
     * Sets the docker client configuration.
     * @param dockerClientConfig
     */
    public void setDockerClientConfig(DockerClientConfig dockerClientConfig) {
        this.dockerClientConfig = dockerClientConfig;
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
}
