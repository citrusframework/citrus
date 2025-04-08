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

package org.citrusframework.agent.plugin.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.okhttp.OkDockerHttpClient;
import org.apache.maven.plugins.annotations.Parameter;

public class DockerConfiguration {

    @Parameter(property = "citrus.agent.docker.enabled", defaultValue = "false")
    private boolean enabled;
    @Parameter(property = "citrus.agent.docker.network")
    private String network;

    private DockerClient dockerClient;

    /**
     * Container image configuration for Citrus agent application.
     */
    @Parameter
    private ImageConfiguration image;

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ImageConfiguration getImage() {
        if (image == null) {
            image = new ImageConfiguration();
        }

        return image;
    }

    public void setImage(ImageConfiguration image) {
        this.image = image;
    }

    public DockerClient getDockerClient() {
        if (dockerClient == null) {
            var dockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
            dockerClient = DockerClientImpl.getInstance(dockerClientConfig, new OkDockerHttpClient.Builder()
                    .dockerHost(dockerClientConfig.getDockerHost())
                    .build());
        }

        return dockerClient;
    }
}
