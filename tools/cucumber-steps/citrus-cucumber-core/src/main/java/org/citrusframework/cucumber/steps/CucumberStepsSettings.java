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

package org.citrusframework.cucumber.steps;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;

import org.citrusframework.kubernetes.ClusterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CucumberStepsSettings {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(CucumberStepsSettings.class);

    private static final String CITRUS_PROPERTY_PREFIX = "citrus.";
    private static final String CITRUS_ENV_PREFIX = "CITRUS_";

    private static final String NAMESPACE_PROPERTY = CITRUS_PROPERTY_PREFIX + "namespace";
    private static final String NAMESPACE_ENV = CITRUS_ENV_PREFIX + "NAMESPACE";
    private static final String NAMESPACE_DEFAULT = "default";

    private static final String CLUSTER_TYPE_PROPERTY = CITRUS_PROPERTY_PREFIX + "cluster.type";
    private static final String CLUSTER_TYPE_ENV = CITRUS_ENV_PREFIX + "CLUSTER_TYPE";
    private static final String CLUSTER_TYPE_DEFAULT = ClusterType.KUBERNETES.name();

    /**
     * Namespace to work on when performing Kubernetes/Knative client operations on resources.
     */
    public static String getDefaultNamespace() {
        String systemNamespace = System.getProperty(NAMESPACE_PROPERTY, System.getenv(NAMESPACE_ENV));

        if (systemNamespace != null) {
            return systemNamespace;
        }

        final File namespace = new File("/var/run/secrets/kubernetes.io/serviceaccount/namespace");
        if (namespace.exists()){
            try {
                return Files.readString(namespace.toPath());
            } catch (IOException e) {
                LOG.warn("Failed to read Kubernetes namespace from filesystem {}", namespace, e);
            }
        }

        return NAMESPACE_DEFAULT;
    }

    /**
     * Cluster type that Citrus is running on.
     */
    public static ClusterType getClusterType() {
        return ClusterType.valueOf(System.getProperty(CLUSTER_TYPE_PROPERTY,
                System.getenv(CLUSTER_TYPE_ENV) != null ? System.getenv(CLUSTER_TYPE_ENV) : CLUSTER_TYPE_DEFAULT).toUpperCase(Locale.US));
    }

    /**
     * True when running on localhost.
     */
    public static boolean isLocal() {
        return isLocal(getClusterType());
    }

    /**
     * True when running on localhost.
     */
    public static boolean isLocal(ClusterType clusterType) {
        return ClusterType.LOCAL.equals(clusterType);
    }

    /**
     * True when running on Openshift.
     */
    public static boolean isOpenshiftCluster() {
        return ClusterType.OPENSHIFT.equals(getClusterType());
    }

    /**
     * True when running on Kubernetes.
     */
    public static boolean isKubernetesCluster() {
        return ClusterType.KUBERNETES.equals(getClusterType());
    }
}
