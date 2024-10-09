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

package org.citrusframework.knative.actions;

import io.fabric8.knative.client.KnativeClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.citrusframework.TestAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.knative.KnativeSettings;
import org.citrusframework.knative.KnativeSupport;
import org.citrusframework.knative.KnativeVariableNames;
import org.citrusframework.kubernetes.ClusterType;
import org.citrusframework.kubernetes.KubernetesSettings;

/**
 * Base action provides access to Knative properties such as broker name. These properties are read from
 * environment settings or explicitly set as part of the test case and get stored as test variables in the current context.
 * This base class gives convenient access to the test variables and provides a fallback if no variable is set.
 */
public interface KnativeAction extends TestAction {

    /**
     * Gets the Kubernetes client.
     * @return
     */
    KubernetesClient getKubernetesClient();

    /**
     * Gets the Knative client.
     * @return
     */
    KnativeClient getKnativeClient();

    /**
     * Gets the current namespace.
     */
    String getNamespace();

    /**
     * Should remove Knative resources automatically after test.
     * @return
     */
    boolean isAutoRemoveResources();

    /**
     * Resolves namespace name from given test context using the stored test variable.
     * Fallback to the namespace given in Knative environment settings when no test variable is present.
     *
     * @param context
     * @return
     */
    default String namespace(TestContext context) {
        if (getNamespace() != null) {
            return getNamespace();
        }

        return KnativeSupport.getNamespace(context);
    }

    /**
     * Resolves the current broker name that has been set in the test context as test variable.
     * Fallback to the broker given in Knative environment settings when no test variable is present.
     *
     * @param brokerName
     * @param context
     * @return
     */
    default String brokerName(String brokerName, TestContext context) {
        if (brokerName != null) {
            return brokerName;
        }

        if (context.getVariables().containsKey(KnativeVariableNames.BROKER_NAME.value())) {
            context.getVariable(KnativeVariableNames.BROKER_NAME.value());
        }

        return KnativeSettings.getBrokerName();
    }

    /**
     * Resolves cluster type from given test context using the stored test variable.
     * Fallback to retrieving the cluster type from environment settings when no test variable is present.
     *
     * @param context
     * @return
     */
    default ClusterType clusterType(TestContext context) {
        if (context.getVariables().containsKey(KnativeVariableNames.CLUSTER_TYPE.value())) {
            Object clusterType = context.getVariableObject(KnativeVariableNames.CLUSTER_TYPE.value());

            if (clusterType instanceof ClusterType) {
                return (ClusterType) clusterType;
            } else {
                return ClusterType.valueOf(clusterType.toString());
            }
        }

        return KubernetesSettings.getClusterType();
    }
}

