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
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ActionBuilder;
import org.citrusframework.actions.ReferenceResolverAwareBuilder;
import org.citrusframework.actions.kubernetes.command.KubernetesCommand;
import org.citrusframework.actions.kubernetes.command.KubernetesEndpointCommandActionBuilder;
import org.citrusframework.actions.kubernetes.command.KubernetesEventCommandActionBuilder;
import org.citrusframework.actions.kubernetes.command.KubernetesInfoCommandActionBuilder;
import org.citrusframework.actions.kubernetes.command.KubernetesNamespaceCommandActionBuilder;
import org.citrusframework.actions.kubernetes.command.KubernetesNodeCommandActionBuilder;
import org.citrusframework.actions.kubernetes.command.KubernetesPodCommandActionBuilder;
import org.citrusframework.actions.kubernetes.command.KubernetesReplicationControllerCommandActionBuilder;
import org.citrusframework.actions.kubernetes.command.KubernetesServiceCommandActionBuilder;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.context.ValidationContext;

public interface KubernetesExecuteActionBuilder<T extends TestAction, B extends KubernetesExecuteActionBuilder<T, B>>
        extends ActionBuilder<T, B>, ReferenceResolverAwareBuilder<T, B>, TestActionBuilder<T> {

    /**
     * Use a custom Kubernetes client.
     */
    B client(Object kubernetesClient);

    /**
     * Use a kubernetes command.
     */
    B command(KubernetesCommand<?, ?, ?> command);

    /**
     * Adds expected command result.
     */
    B result(String result);

    /**
     * Adds JsonPath command result validation.
     */
    B validate(String path, Object value);

    B validator(MessageValidator<? extends ValidationContext> validator);

    B pathExpressionValidator(MessageValidator<? extends ValidationContext> validator);

    /**
     * Use an info command.
     */
    KubernetesInfoCommandActionBuilder<?, ?, ?, ?> info();

    /**
     * Pods action builder.
     */
    KubernetesPodCommandActionBuilder<?> pods();

    /**
     * Services action builder.
     */
    KubernetesServiceCommandActionBuilder<?> services();

    /**
     * ReplicationControllers action builder.
     */
    KubernetesReplicationControllerCommandActionBuilder<?> replicationControllers();

    /**
     * Endpoints action builder.
     */
    KubernetesEndpointCommandActionBuilder<?> endpoints();

    /**
     * Nodes action builder.
     */
    KubernetesNodeCommandActionBuilder<?> nodes();

    /**
     * Events action builder.
     */
    KubernetesEventCommandActionBuilder<?> events();

    /**
     * Namespaces action builder.
     */
    KubernetesNamespaceCommandActionBuilder<?> namespaces();
}
