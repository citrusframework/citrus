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

package org.citrusframework.kubernetes.command;

import io.fabric8.kubernetes.api.model.HasMetadata;
import org.citrusframework.kubernetes.client.KubernetesClient;

/**
 * Command executes an operation on the kubernetes client (e.g. list, create).
 * Type parameter:
 * <T> the Kubernetes resource that this command operates with (e.g. Pod, Service, Secret, ...)
 * <O> the command output that may be validated (e.g. Pod, PodList, InfoResult, ...)
 * @since 2.7
 */
public interface KubernetesCommand<T extends HasMetadata, O> extends org.citrusframework.actions.kubernetes.command.KubernetesCommand<T, O, KubernetesClient> {

}
