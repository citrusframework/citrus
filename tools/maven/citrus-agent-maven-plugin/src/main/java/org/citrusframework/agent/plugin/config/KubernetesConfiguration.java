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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import io.fabric8.kubernetes.api.model.NamedContext;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.citrusframework.util.StringUtils;

public class KubernetesConfiguration {

    @Parameter(property = "citrus.agent.kubernetes.enabled", defaultValue = "false")
    private boolean enabled;
    @Parameter(property = "citrus.agent.kubernetes.namespace")
    private String namespace;

    private KubernetesClient k8s;

    /**
     * Container image configuration for Citrus agent application.
     */
    @Parameter
    private ImageConfiguration image;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
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
            image.setRegistry("localhost:5001");
        }

        return image;
    }

    public void setImage(ImageConfiguration image) {
        this.image = image;
    }

    /**
     * Namespace retrieved from Maven plugin configuration.
     * When namespace is not set fallback to Kubernetes client configuration and the current namespace in use.
     */
    public String getNamespace(KubernetesClient k8s, Log log) {
        if (namespace != null && !namespace.isEmpty()) {
            return namespace;
        }

        final File namespace = new File("/var/run/secrets/kubernetes.io/serviceaccount/namespace");
        if (namespace.exists()){
            try {
                return Files.readString(namespace.toPath());
            } catch (IOException e) {
                log.warn("Failed to read Kubernetes namespace from filesystem %s".formatted(namespace), e);
            }
        }

        NamedContext currentContext = k8s.getConfiguration().getCurrentContext();
        if (currentContext != null && currentContext.getContext() != null && StringUtils.hasText(currentContext.getContext().getNamespace())) {
            log.debug("Reading current namespace from context: %s".formatted(currentContext.getName()));
            return currentContext.getContext().getNamespace();
        }

        return "default";
    }

    public KubernetesClient getKubernetesClient() {
        if (k8s == null) {
            k8s = new KubernetesClientBuilder().build();
        }

        return k8s;
    }
}
