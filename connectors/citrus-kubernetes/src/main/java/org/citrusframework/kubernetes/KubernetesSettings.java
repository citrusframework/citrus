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

package org.citrusframework.kubernetes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.fabric8.kubernetes.api.model.NamedContext;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubernetesSettings {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(KubernetesSettings.class);

    private static final String KUBERNETES_PROPERTY_PREFIX = "citrus.kubernetes.";
    private static final String KUBERNETES_ENV_PREFIX = "CITRUS_KUBERNETES_";

    private static final String AUTO_REMOVE_RESOURCES_PROPERTY = KUBERNETES_PROPERTY_PREFIX + "auto.remove.resources";
    private static final String AUTO_REMOVE_RESOURCES_ENV = KUBERNETES_ENV_PREFIX + "AUTO_REMOVE_RESOURCES";
    private static final String AUTO_REMOVE_RESOURCES_DEFAULT = "true";

    private static final String ENABLED_PROPERTY = KUBERNETES_PROPERTY_PREFIX + "enabled";
    private static final String ENABLED_ENV = KUBERNETES_ENV_PREFIX + "ENABLED";
    private static final String ENABLED_DEFAULT = "true";

    private static final String USE_DEFAULT_ACTOR_PROPERTY = KUBERNETES_PROPERTY_PREFIX + "use.default.actor";
    private static final String USE_DEFAULT_ACTOR_ENV = KUBERNETES_ENV_PREFIX + "USE_DEFAULT_ACTOR";
    private static final String USE_DEFAULT_ACTOR_DEFAULT = "false";

    private static final String AUTO_CREATE_SERVER_BINDING_PROPERTY = KUBERNETES_PROPERTY_PREFIX + "auto.create.server.binding";
    private static final String AUTO_CREATE_SERVER_BINDING_ENV = KUBERNETES_ENV_PREFIX + "AUTO_CREATE_SERVER_BINDING";
    private static final String AUTO_CREATE_SERVER_BINDING_DEFAULT = "true";

    private static final String CLUSTER_TYPE_PROPERTY = KUBERNETES_PROPERTY_PREFIX + "cluster.type";
    private static final String CLUSTER_TYPE_ENV = KUBERNETES_ENV_PREFIX + "CLUSTER_TYPE";
    private static final String CLUSTER_TYPE_DEFAULT = ClusterType.KUBERNETES.name();

    private static final String CLUSTER_WILDCARD_DOMAIN_PROPERTY = KUBERNETES_PROPERTY_PREFIX + "cluster.wildcard.domain";
    private static final String CLUSTER_WILDCARD_DOMAIN_ENV = KUBERNETES_ENV_PREFIX + "CLUSTER_WILDCARD_DOMAIN";
    public static final String DEFAULT_DOMAIN_SUFFIX = "svc.cluster.local";

    private static final String SERVICE_TIMEOUT_PROPERTY = KUBERNETES_PROPERTY_PREFIX + "service.timeout";
    private static final String SERVICE_TIMEOUT_ENV = KUBERNETES_ENV_PREFIX + "SERVICE_TIMEOUT";
    private static final String SERVICE_TIMEOUT_DEFAULT = "2000";

    private static final String CONNECT_TIMEOUT_PROPERTY = KUBERNETES_PROPERTY_PREFIX + "connect.timeout";
    private static final String CONNECT_TIMEOUT_ENV = KUBERNETES_ENV_PREFIX + "CONNECT_TIMEOUT";
    private static final String CONNECT_TIMEOUT_DEFAULT = "5000";

    private static final String NAMESPACE_PROPERTY = KUBERNETES_PROPERTY_PREFIX + "namespace";
    private static final String NAMESPACE_ENV = KUBERNETES_ENV_PREFIX + "NAMESPACE";
    private static final String NAMESPACE_DEFAULT = "default";

    private static final String TEST_ID_LABEL_PROPERTY = KUBERNETES_PROPERTY_PREFIX + "test.id.label";
    private static final String TEST_ID_LABEL_ENV = KUBERNETES_ENV_PREFIX + "TEST_ID_LABEL";
    private static final String TEST_ID_LABEL_DEFAULT = "citrusframework.org/test-id";

    private static final String API_VERSION_PROPERTY = KUBERNETES_PROPERTY_PREFIX + "api.version";
    private static final String API_VERSION_ENV = KUBERNETES_ENV_PREFIX + "API_VERSION";
    private static final String API_VERSION_DEFAULT = "v1";

    private static final String SERVICE_NAME_PROPERTY = KUBERNETES_PROPERTY_PREFIX + "service.name";
    private static final String SERVICE_NAME_ENV = KUBERNETES_ENV_PREFIX + "SERVICE_NAME";
    private static final String SERVICE_NAME_DEFAULT = "citrus-k8s-service";

    private static final String SERVICE_PORT_PROPERTY = KUBERNETES_PROPERTY_PREFIX + "service.port";
    private static final String SERVICE_PORT_ENV = KUBERNETES_ENV_PREFIX + "SERVICE_PORT";
    private static final String SERVICE_PORT_DEFAULT = "8080";

    private static final String DEFAULT_LABELS_PROPERTY = KUBERNETES_PROPERTY_PREFIX + "default.labels";
    private static final String DEFAULT_LABELS_ENV = KUBERNETES_ENV_PREFIX + "DEFAULT_LABELS";
    private static final String DEFAULT_LABELS_DEFAULT = "app=citrus";

    private static final String MAX_ATTEMPTS_PROPERTY = KUBERNETES_PROPERTY_PREFIX + "max.attempts";
    private static final String MAX_ATTEMPTS_ENV = KUBERNETES_ENV_PREFIX + "MAX_ATTEMPTS";
    private static final String MAX_ATTEMPTS_DEFAULT = "150";

    private static final String DELAY_BETWEEN_ATTEMPTS_PROPERTY = KUBERNETES_PROPERTY_PREFIX + "delay.between.attempts";
    private static final String DELAY_BETWEEN_ATTEMPTS_ENV = KUBERNETES_ENV_PREFIX + "DELAY_BETWEEN_ATTEMPTS";
    private static final String DELAY_BETWEEN_ATTEMPTS_DEFAULT = "2000";

    private static final String PRINT_POD_LOGS_PROPERTY = KUBERNETES_PROPERTY_PREFIX + "print.pod.logs";
    private static final String PRINT_POD_LOGS_ENV = KUBERNETES_ENV_PREFIX + "PRINT_POD_LOGS";
    private static final String PRINT_POD_LOGS_DEFAULT = "true";

    private static final String WATCH_LOGS_TIMEOUT_PROPERTY = KUBERNETES_PROPERTY_PREFIX + "watch.logs.timeout";
    private static final String WATCH_LOGS_TIMEOUT_ENV = KUBERNETES_ENV_PREFIX + "WATCH_LOGS_TIMEOUT";
    private static final String WATCH_LOGS_TIMEOUT_DEFAULT = "60000";

    private KubernetesSettings() {
        // prevent instantiation of utility class
    }

    /**
     * Request timeout when receiving cloud events.
     * @return
     */
    public static long getServiceTimeout() {
        return Long.parseLong(System.getProperty(SERVICE_TIMEOUT_PROPERTY,
                System.getenv(SERVICE_TIMEOUT_ENV) != null ? System.getenv(SERVICE_TIMEOUT_ENV) : SERVICE_TIMEOUT_DEFAULT));
    }

    /**
     * Timeout when connecting to Kubernetes cluster.
     * @return
     */
    public static long getConnectTimeout() {
        return Long.parseLong(System.getProperty(CONNECT_TIMEOUT_PROPERTY,
                System.getenv(CONNECT_TIMEOUT_ENV) != null ? System.getenv(CONNECT_TIMEOUT_ENV) : CONNECT_TIMEOUT_DEFAULT));
    }

    /**
     * Namespace to work on when performing Kubernetes client operations such as creating triggers, services and so on.
     * @return
     */
    public static String getNamespace() {
        String systemNamespace = System.getProperty(NAMESPACE_PROPERTY, System.getenv(NAMESPACE_ENV));

        if (systemNamespace != null) {
            return systemNamespace;
        }

        final File namespace = new File("/var/run/secrets/kubernetes.io/serviceaccount/namespace");
        if (namespace.exists()){
            try {
                return Files.readString(namespace.toPath());
            } catch (IOException e) {
                logger.warn("Failed to read Kubernetes namespace from filesystem {}", namespace, e);
            }
        }

        try (final KubernetesClient k8s = new KubernetesClientBuilder().build()) {
            NamedContext currentContext = k8s.getConfiguration().getCurrentContext();
            if (currentContext != null && currentContext.getContext() != null && StringUtils.hasText(currentContext.getContext().getNamespace())) {
                logger.debug("Reading current namespace from context: {}", currentContext.getName());
                return currentContext.getContext().getNamespace();
            }
        }

        return NAMESPACE_DEFAULT;
    }

    /**
     * Cluster wildcard domain or default if non is set.
     * @return
     */
    public static String getClusterWildcardDomain() {
        return System.getProperty(CLUSTER_WILDCARD_DOMAIN_PROPERTY,
                System.getenv(CLUSTER_WILDCARD_DOMAIN_ENV) != null ? System.getenv(CLUSTER_WILDCARD_DOMAIN_ENV) : getNamespace() + "." + DEFAULT_DOMAIN_SUFFIX);
    }

    /**
     * Cluster type that YAKS is running on.
     * @return
     */
    public static ClusterType getClusterType() {
        return ClusterType.valueOf(System.getProperty(CLUSTER_TYPE_PROPERTY,
                System.getenv(CLUSTER_TYPE_ENV) != null ? System.getenv(CLUSTER_TYPE_ENV) : CLUSTER_TYPE_DEFAULT).toUpperCase(Locale.US));
    }

    /**
     * Api version for current Kubernetes installation.
     * @return
     */
    public static String getApiVersion() {
        return System.getProperty(API_VERSION_PROPERTY,
                System.getenv(API_VERSION_ENV) != null ? System.getenv(API_VERSION_ENV) : API_VERSION_DEFAULT);
    }

    /**
     * Service name to use when creating a new service for cloud event subscriptions.
     * @return
     */
    public static String getServiceName() {
        return System.getProperty(SERVICE_NAME_PROPERTY,
                System.getenv(SERVICE_NAME_ENV) != null ? System.getenv(SERVICE_NAME_ENV) : SERVICE_NAME_DEFAULT);
    }

    /**
     * Service port used when consuming cloud events via Http.
     * @return
     */
    public static int getServicePort() {
        return Integer.parseInt(System.getProperty(SERVICE_PORT_PROPERTY,
                System.getenv(SERVICE_PORT_ENV) != null ? System.getenv(SERVICE_PORT_ENV) : SERVICE_PORT_DEFAULT));
    }

    /**
     * Read labels for Kubernetes resources created by the test. The environment setting should be a
     * comma delimited list of key-value pairs.
     * @return
     */
    public static Map<String, String> getDefaultLabels() {
        String labelsConfig = System.getProperty(DEFAULT_LABELS_PROPERTY,
                System.getenv(DEFAULT_LABELS_ENV) != null ? System.getenv(DEFAULT_LABELS_ENV) : DEFAULT_LABELS_DEFAULT);

        return Stream.of(labelsConfig.split(","))
                .map(item -> item.split("=", 2))
                .filter(keyValue -> keyValue.length == 2)
                .collect(Collectors.toMap(item -> item[0], item -> item[1]));
    }

    /**
     * Should bind each Kubernetes service created to a local Http server instance.
     * @return
     */
    public static boolean isAutoCreateServerBinding() {
        return Boolean.parseBoolean(System.getProperty(AUTO_CREATE_SERVER_BINDING_PROPERTY,
                System.getenv(AUTO_CREATE_SERVER_BINDING_ENV) != null ? System.getenv(AUTO_CREATE_SERVER_BINDING_ENV) :
                        AUTO_CREATE_SERVER_BINDING_DEFAULT));
    }

    /**
     * Kubernetes may be disabled by default.
     * @return
     */
    public static boolean isEnabled() {
        return Boolean.parseBoolean(System.getProperty(ENABLED_PROPERTY,
                System.getenv(ENABLED_ENV) != null ? System.getenv(ENABLED_ENV) : ENABLED_DEFAULT));
    }

    /**
     * When set to true Kubernetes resources (e.g. services) created during the test are
     * automatically removed after the test.
     * @return
     */
    public static boolean isAutoRemoveResources() {
        return Boolean.parseBoolean(System.getProperty(AUTO_REMOVE_RESOURCES_PROPERTY,
                System.getenv(AUTO_REMOVE_RESOURCES_ENV) != null ? System.getenv(AUTO_REMOVE_RESOURCES_ENV) : AUTO_REMOVE_RESOURCES_DEFAULT));
    }

    /**
     * When set to true test will print pod logs e.g. while waiting for a pod log message.
     * @return
     */
    public static boolean isPrintPodLogs() {
        return Boolean.parseBoolean(System.getProperty(PRINT_POD_LOGS_PROPERTY,
                System.getenv(PRINT_POD_LOGS_ENV) != null ? System.getenv(PRINT_POD_LOGS_ENV) : PRINT_POD_LOGS_DEFAULT));
    }

    /**
     * Maximum number of attempts when polling for running state and log messages.
     * @return
     */
    public static int getMaxAttempts() {
        return Integer.parseInt(System.getProperty(MAX_ATTEMPTS_PROPERTY,
                System.getenv(MAX_ATTEMPTS_ENV) != null ? System.getenv(MAX_ATTEMPTS_ENV) : MAX_ATTEMPTS_DEFAULT));
    }

    /**
     * Delay in milliseconds to wait after polling attempt.
     * @return
     */
    public static long getDelayBetweenAttempts() {
        return Long.parseLong(System.getProperty(DELAY_BETWEEN_ATTEMPTS_PROPERTY,
                System.getenv(DELAY_BETWEEN_ATTEMPTS_ENV) != null ? System.getenv(DELAY_BETWEEN_ATTEMPTS_ENV) : DELAY_BETWEEN_ATTEMPTS_DEFAULT));
    }

    /**
     * Duration in milliseconds to watch pod logs.
     * @return
     */
    public static long getWatchLogsTimeout() {
        return Long.parseLong(System.getProperty(WATCH_LOGS_TIMEOUT_PROPERTY,
                System.getenv(WATCH_LOGS_TIMEOUT_ENV) != null ? System.getenv(WATCH_LOGS_TIMEOUT_ENV) : WATCH_LOGS_TIMEOUT_DEFAULT));
    }

    /**
     * True when running on localhost.
     * @return
     */
    public static boolean isLocal() {
        return isLocal(getClusterType());
    }

    /**
     * True when running on localhost.
     * @return
     */
    public static boolean isLocal(ClusterType clusterType) {
        return ClusterType.LOCAL.equals(clusterType);
    }

    /**
     * True when running on Openshift.
     * @return
     */
    public static boolean isOpenshiftCluster() {
        return ClusterType.OPENSHIFT.equals(getClusterType());
    }

    /**
     * True when running on Kubernetes.
     * @return
     */
    public static boolean isKubernetesCluster() {
        return ClusterType.KUBERNETES.equals(getClusterType());
    }

    public static String getTestIdLabel() {
        return System.getProperty(TEST_ID_LABEL_PROPERTY,
                System.getenv(TEST_ID_LABEL_ENV) != null ? System.getenv(TEST_ID_LABEL_ENV) : TEST_ID_LABEL_DEFAULT);
    }

    /**
     * True when all Kubernetes test actions should use a default test actor.
     * @return
     */
    public static boolean isUseDefaultKubernetesActor() {
        return Boolean.parseBoolean(System.getProperty(USE_DEFAULT_ACTOR_PROPERTY,
                System.getenv(USE_DEFAULT_ACTOR_ENV) != null ? System.getenv(USE_DEFAULT_ACTOR_ENV) : USE_DEFAULT_ACTOR_DEFAULT));
    }
}
