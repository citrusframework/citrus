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

package org.citrusframework.actions.kubernetes;

import org.citrusframework.TestAction;

public interface KubernetesPodActionBuilder<T extends TestAction> {

    /**
     * Verify that given pod is running.
     * @param podName the name of the Camel K pod.
     */
    KubernetesPodVerifyActionBuilder<?, ?> verify(String podName);

    /**
     * Watch pod logs for given pod identified by its name.
     * @param podName the name of the Camel K pod.
     */
    KubernetesPodWatchLogsActionBuilder<?, ?> watchLogs(String podName);

    /**
     * Watch pod logs for given pod identified by label selector.
     * @param label the name of the pod label to filter on.
     * @param value the value of the pod label to match.
     */
    KubernetesPodWatchLogsActionBuilder<?, ?> watchLogs(String label, String value);

    /**
     * Add annotation on pod instance.
     * @param podName the name of the Kubernetes pod.
     */
    KubernetesCreateAnnotationsActionBuilder<?, ?> addAnnotation(String podName);

    /**
     * Add label on pod instance.
     * @param podName the name of the Kubernetes pod.
     */
    KubernetesCreateLabelsActionBuilder<?, ?> addLabel(String podName);

    /**
     * Verify that given pod is running.
     * @param label the name of the pod label to filter on.
     * @param value the value of the pod label to match.
     */
    KubernetesPodVerifyActionBuilder<?, ?> verify(String label, String value);

    /**
     * Delete all pods in current namespace.
     */
    KubernetesPodDeleteActionBuilder<?, ?> delete();

    /**
     * Delete given pod by its name in current namespace.
     */
    KubernetesPodDeleteActionBuilder<?, ?> delete(String podName);

    /**
     * Delete given pod identified by the label value expression.
     */
    KubernetesPodDeleteActionBuilder<?, ?> delete(String label, String value);
}
