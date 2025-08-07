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

package org.citrusframework.actions.kubernetes.command;

import org.citrusframework.spi.Resource;

public interface KubernetesPodCommandActionBuilder<R> {

    /**
     * List pods.
     */
    KubernetesNamespacedCommandActionBuilder<?, R, ? extends KubernetesListCommandResult<R>, ?> list();

    /**
     * Creates new pod.
     */
    KubernetesNamedCommandActionBuilder<?, R, R, ?> create(R pod);

    /**
     * Create new pod from template.
     */
    KubernetesNamedCommandActionBuilder<?, R, R, ?> create(Resource template);

    /**
     * Create new pod from template path.
     */
    KubernetesNamedCommandActionBuilder<?, R, R, ?> create(String templatePath);

    /**
     * Gets pod by name.
     */
    KubernetesNamedCommandActionBuilder<?, R, R, ?> get(String name);

    /**
     * Deletes pod by name.
     */
    KubernetesNamedCommandActionBuilder<?, R, ? extends KubernetesDeleteCommandResult, ?> delete(String name);

    /**
     * Deletes pods.
     */
    KubernetesNamedCommandActionBuilder<?, R, ? extends KubernetesDeleteCommandResult, ?> delete();

    /**
     * Watch pods.
     */
    KubernetesNamedCommandActionBuilder<?, R, ? extends KubernetesWatchCommandResult<R, ?, ?>, ?> watch();
}
